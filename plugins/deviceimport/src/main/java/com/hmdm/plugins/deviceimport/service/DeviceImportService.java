/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.plugins.deviceimport.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.*;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.domain.Settings;
import com.hmdm.plugins.deviceimport.rest.json.DeviceImportConfirmRequest;
import com.hmdm.plugins.deviceimport.rest.json.DeviceImportRequest;
import com.hmdm.plugins.deviceimport.rest.json.DeviceImportResult;
import com.hmdm.plugins.deviceimport.rest.json.DeviceImportStatusItem;
import com.hmdm.plugins.deviceimport.rest.json.DeviceListParsingResult;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.security.SecurityException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hmdm.util.FileUtil.writeToFile;

/**
 * <p>A service used for supporting device import process.</p>
 *
 * @author isv
 */
@Singleton
public class DeviceImportService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceImportService.class);
    private static final int ALIVE_TIME_AMOUNT = 3;
    private static final TimeUnit ALIVE_TIME_UNIT = TimeUnit.HOURS;

    private final GroupDAO groupDAO;
    private final ConfigurationDAO configurationDAO;
    private final DeviceDAO deviceDAO;
    private final UnsecureDAO unsecureDAO;
    private final CommonDAO commonDAO;

    /**
     * <p>A mapping from unique UUIDs to paths to files uploaded to server.</p>
     */
    private static final Cache<String, String> uploadedFilesPaths = CacheBuilder.newBuilder()
            .initialCapacity(5)
            .maximumSize(50)
            .expireAfterAccess(ALIVE_TIME_AMOUNT, ALIVE_TIME_UNIT)
            .removalListener((RemovalListener<String, String>) notification -> {
                final String fileAbsolutePath = notification.getValue();
                try {
                    final boolean deleted = Files.deleteIfExists(new File(fileAbsolutePath).toPath());
                    if (deleted) {
                        logger.debug("Deleted temporary Excel file: {}", fileAbsolutePath);
                    }
                } catch (IOException e) {
                    logger.error("Failed to delete temporary Excel file: {}", fileAbsolutePath, e);
                }

            })
            .build();

    /**
     * <p>A mapping from unique UUIDs to results of parsing the Excel file.</p>
     */
    private static final Cache<String, List<DeviceImportStatusItem>> uploadedFilesParsingResults = CacheBuilder.newBuilder()
            .initialCapacity(5)
            .maximumSize(50)
            .expireAfterAccess(ALIVE_TIME_AMOUNT, ALIVE_TIME_UNIT)
            .build();

    /**
     * <p>A mapping from unique UUIDs to requests of parsing the Excel file.</p>
     */
    private static final Cache<String, DeviceImportRequest> importRequests = CacheBuilder.newBuilder()
            .initialCapacity(5)
            .maximumSize(50)
            .expireAfterAccess(ALIVE_TIME_AMOUNT, ALIVE_TIME_UNIT)
            .build();

    /**
     * <p>Constructs new <code>DeviceImportService</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceImportService(GroupDAO groupDAO, ConfigurationDAO configurationDAO, DeviceDAO deviceDAO,
                               UnsecureDAO unsecureDAO, CommonDAO commonDAO) {
        this.groupDAO = groupDAO;
        this.configurationDAO = configurationDAO;
        this.deviceDAO = deviceDAO;
        this.unsecureDAO = unsecureDAO;
        this.commonDAO = commonDAO;
    }

    /**
     * <p>Uploads the file to server. Returns a reference to path for uploaded file.</p>
     *
     * @param uploadedInputStream an input stream providing the content of the file to upload.
     * @param fileDetail the details for uploaded file.
     * @return a mapping for filename and file ID.
     * @throws IOException if an I/O error occurs while storing uploaded file on file system.
     */
    public Map<String, String> onFileUploaded(InputStream uploadedInputStream,
                                              FormDataContentDisposition fileDetail) throws IOException {
        // For some reason, the browser sends the file name in ISO_8859_1, so we use a workaround to convert
        // it to UTF_8 and enable non-ASCII characters
        // https://stackoverflow.com/questions/50582435/jersey-filename-encoded
        String fileName = new String(fileDetail.getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        File uploadFile = File.createTempFile(fileName, "temp");
        writeToFile(uploadedInputStream, uploadFile.getAbsolutePath());

        final ConcurrentMap<String, String> uploadedFilesPathsMap = uploadedFilesPaths.asMap();

        String uuid;
        do {
            uuid = UUID.randomUUID().toString();
        } while (uploadedFilesPathsMap.putIfAbsent(uuid, uploadFile.getAbsolutePath()) != null);

        Map<String, String> result = new HashMap<>();

        result.put("uuid", uuid);
        result.put("fileName", fileName);

        return result;
    }

    /**
     * <p>Parses the uploaded file and analyzes the list of extracted devices to be imported into DB.</p>
     *
     * @param request the parameters of device import process.
     * @return the results of parsing and analyzing the details for devices to be imported.
     */
    public DeviceListParsingResult prepareImport(DeviceImportRequest request) throws IOException {

        // Verify that selected group and configuration belong to this customer account
        if (request.getGroupId() != null) {
            try {
                groupDAO.getGroupById(request.getGroupId());
            } catch (SecurityException e) {
                throw new DeviceImportServiceException("plugin.deviceimport.error.group.invalid");
            }
        }
        try {
            configurationDAO.getConfigurationById(request.getConfigurationId());
        } catch (SecurityException e) {
            throw new DeviceImportServiceException("plugin.deviceimport.error.configuration.invalid");
        }

        final InputStream deviceListContent;
        final DeviceListParser deviceListParser;
        switch (request.getImportType()) {
            case XLSX: {
                deviceListParser = new DeviceListXLSXParser(deviceDAO);
                deviceListContent = getFileContentStream(request);
                break;
            }
            case CSV: {
                deviceListParser = new DeviceListCSVParser(deviceDAO);
                deviceListContent = getFileContentStream(request);
                break;
            }
            case LIST: {
                deviceListParser = new DeviceListCSVParser(deviceDAO);
                deviceListContent = new ByteArrayInputStream(request.getListContent().getBytes());
                break;
            }
            default: throw new DeviceImportServiceException("plugin.deviceimport.error.import.type.invalid");
        }

        // Parse file or list content
        final List<DeviceImportStatusItem> items = deviceListParser.parseDeviceList(request, deviceListContent);

        // Put the parsing results to mapping for further use by next steps of device import process
        final ConcurrentMap<String, List<DeviceImportStatusItem>> uploadedFilesParsingResultsMap
                = uploadedFilesParsingResults.asMap();
        String uuid;
        do {
            uuid = UUID.randomUUID().toString();
        } while (uploadedFilesParsingResultsMap.putIfAbsent(uuid, items) != null);

        importRequests.put(uuid, request);

        return new DeviceListParsingResult(uuid, items);
    }

    /**
     * <p>Imports the devices from the previously parsed Excel file to database.</p>
     *
     * @param request the parameters of device import process.
     * @return the results of devices import.
     */
    @Transactional
    public DeviceImportResult importDevices(DeviceImportConfirmRequest request) {
        final String processId = request.getUuid();
        final boolean overrideExisting = (request.getExistingMode() == 1);
        final List<DeviceImportStatusItem> parsedItems = uploadedFilesParsingResults.getIfPresent(processId);
        final DeviceImportRequest importParameters = importRequests.getIfPresent(processId);
        Settings settings = new Settings();
        if (!unsecureDAO.isSingleCustomer()) {
            commonDAO.loadCustomerSettings(settings);
        }

        if (parsedItems == null || importParameters == null) {
            throw new DeviceImportServiceException("plugin.deviceimport.error.file.expired");
        } else {
            AtomicInteger newDevicesCount = new AtomicInteger(0);
            AtomicInteger existingDevicesUpdated = new AtomicInteger(0);
            AtomicInteger existingDevicesSkipped = new AtomicInteger(0);
            AtomicInteger repeatedRecordsSkipped = new AtomicInteger(0);
            AtomicInteger devicesSkippedByLicense = new AtomicInteger(0);

            Set<String> processedDeviceNumbers = new HashSet<>();

            parsedItems.forEach(item -> {
                final String deviceNumberLowerCase = item.getDeviceNumber().toLowerCase();
                if (processedDeviceNumbers.contains(deviceNumberLowerCase)) {
                    repeatedRecordsSkipped.incrementAndGet();
                } else {
                    processedDeviceNumbers.add(deviceNumberLowerCase);
                    try {
                        final Device device = this.deviceDAO.getDeviceByNumberIgnoreCase(item.getDeviceNumber());
                        if (device != null) {
                            if (!device.getId().equals(item.getExistingDeviceId())) {
                                logger.warn("The device {} has changed it's unique ID in DB between the parsing of " +
                                                "Excel file and importing the devices into DB." +
                                                " The ID at file parsing time was #{}, " +
                                                "the ID at device import time is #{}",
                                        item.getDeviceNumber(), item.getExistingDeviceId(), device.getId());
                            }

                            if (overrideExisting) {
                                device.setImei(item.getImei());
                                device.setPhone(item.getPhoneNumber());
                                device.setDescription(item.getDescription());
                                device.setCustom1(item.getCustom1());
                                device.setCustom2(item.getCustom2());
                                device.setCustom3(item.getCustom3());
                                device.setConfigurationId(importParameters.getConfigurationId());
                                if (importParameters.getGroupId() != null) {
                                    LookupItem group = new LookupItem(importParameters.getGroupId(), null);
                                    device.setGroups(Collections.singletonList(group));
                                }

                                this.deviceDAO.updateDevice(device);

                                existingDevicesUpdated.incrementAndGet();
                            } else {
                                existingDevicesSkipped.incrementAndGet();
                            }
                        } else {
                            if (settings.getDeviceLimit() == 0 ||
                                    newDevicesCount.get() < settings.getDeviceLimit() - settings.getDeviceCount()) {

                                Device newDevice = new Device();
                                newDevice.setNumber(item.getDeviceNumber());
                                newDevice.setImei(item.getImei());
                                newDevice.setPhone(item.getPhoneNumber());
                                newDevice.setDescription(item.getDescription());
                                newDevice.setCustom1(item.getCustom1());
                                newDevice.setCustom2(item.getCustom2());
                                newDevice.setCustom3(item.getCustom3());
                                newDevice.setConfigurationId(importParameters.getConfigurationId());
                                newDevice.setLastUpdate(0L);

                                if (importParameters.getGroupId() != null) {
                                    LookupItem group = new LookupItem(importParameters.getGroupId(), null);
                                    newDevice.setGroups(Collections.singletonList(group));
                                }

                                this.deviceDAO.insertDevice(newDevice);

                                newDevicesCount.incrementAndGet();

                                logger.info("Adding new device {} during device import", newDevice.getNumber());
                            } else {
                                devicesSkippedByLicense.incrementAndGet();
                                logger.info("New device {} not added due to the license limit", item.getDeviceNumber());
                            }
                        }
                    } catch (SecurityException e) {
                        logger.error("Device {} belongs to a different customer", item.getDeviceNumber());
                    } catch (PersistenceException e) {
                        logger.error("Got an SQL error when importing device {}. Terminating entire import process.",
                                item.getDeviceNumber(), e);
                        throw e;
                    } catch (Exception e) {
                        logger.error("Skipping importing the device {} due to unexpected error", item.getDeviceNumber(), e);
                    }
                }
            });

            DeviceImportResult result = new DeviceImportResult();
            result.setNewDevicesCount(newDevicesCount.get());
            result.setExistingDevicesUpdated(existingDevicesUpdated.get());
            result.setExistingDevicesSkipped(existingDevicesSkipped.get());
            result.setRepeatedRecordsSkipped(repeatedRecordsSkipped.get());
            result.setDevicesSkippedByLicense(devicesSkippedByLicense.get());

            return result;
        }
    }

    /**
     * <p>Gets the content of the file with device list mapped to specified request for device import.</p>
     *
     * @param request the parameters for the device import process.
     * @return an input stream with content of the device list.
     * @throws FileNotFoundException if file with device list content is not found.
     */
    private static InputStream getFileContentStream(DeviceImportRequest request) throws FileNotFoundException {
        final String filePathId = request.getFilePathId();
        final String filePath = uploadedFilesPaths.getIfPresent(filePathId);
        if (filePath == null) {
            throw new DeviceImportServiceException("plugin.deviceimport.error.file.expired");
        } else {
            final File deviceListFile = new File(filePath);
            if (!deviceListFile.exists() || !deviceListFile.isFile()) {
                throw new DeviceImportServiceException("plugin.deviceimport.error.file.expired");
            } else {
                return new FileInputStream(deviceListFile);
            }
        }
    }
}
