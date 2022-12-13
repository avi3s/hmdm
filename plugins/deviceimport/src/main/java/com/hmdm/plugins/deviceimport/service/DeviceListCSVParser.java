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

import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.plugins.deviceimport.rest.json.DeviceImportRequest;
import com.hmdm.plugins.deviceimport.rest.json.DeviceImportStatusItem;
import com.hmdm.security.SecurityException;
import liquibase.util.csv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * <p>A parser for imported device data using format of CSV files.</p>
 */
public class DeviceListCSVParser implements DeviceListParser {

    private static final Logger logger = LoggerFactory.getLogger(DeviceListCSVParser.class);

    /**
     * <p>An interface to device data in DB.</p>
     */
    private final DeviceDAO deviceDAO;

    /**
     * <p>Constructs new <code>DeviceListCSVParser</code> instance. This implementation does nothing.</p>
     */
    public DeviceListCSVParser(DeviceDAO deviceDAO) {
        this.deviceDAO = deviceDAO;
    }

    /**
     * <p>Parses the uploaded list of devices and analyzes the list of extracted devices to be imported into DB.</p>
     *
     * @param request            the parameters of device import process.
     * @param devicesListContent the content of the list with devices data.
     * @return the results of parsing and analyzing the details for devices to be imported.
     */
    @Override
    public List<DeviceImportStatusItem> parseDeviceList(DeviceImportRequest request, InputStream devicesListContent) throws IOException {
        // Parse file
        final Map<String, AtomicInteger> deviceNumberCounts = new HashMap<>();
        final List<DeviceImportStatusItem> items = new ArrayList<>();

        final Function<String[], String> deviceNumberExtractor = row -> {
            final int deviceNumberIndex = request.getDeviceNumberColumnIndex();
            if (deviceNumberIndex > 0 && deviceNumberIndex <= row.length) {
                return row[request.getDeviceNumberColumnIndex() - 1];
            }
            return null;
        };
        
        final Function<String[], String> imeiExtractor = row -> {
            if (request.getImeiColumnIndex() != null) {
                final int imeiColumnIndex = request.getImeiColumnIndex();
                if (imeiColumnIndex > 0 && imeiColumnIndex <= row.length) {
                    return row[imeiColumnIndex - 1];
                }
            }
            return null;
        };
        
        final Function<String[], String> phoneNumberExtractor = row -> {
            if (request.getPhoneNumberColumnIndex() != null) {
                final int phoneColumnIndex = request.getPhoneNumberColumnIndex();
                if (phoneColumnIndex > 0 && phoneColumnIndex <= row.length) {
                    return row[phoneColumnIndex - 1];
                }
            }
            return null;
        };


        final Function<String[], String> descriptionExtractor = row -> {
            if (request.getDescriptionColumnIndex() != null) {
                final int descriptionColumnIndex = request.getDescriptionColumnIndex();
                if (descriptionColumnIndex > 0 && descriptionColumnIndex <= row.length) {
                    return row[descriptionColumnIndex - 1];
                }
            }
            return null;
        };

        final Function<String[], String> custom1Extractor = row -> {
            if (request.getCustom1ColumnIndex() != null) {
                final int custom1ColumnIndex = request.getCustom1ColumnIndex();
                if (custom1ColumnIndex > 0 && custom1ColumnIndex <= row.length) {
                    return row[custom1ColumnIndex - 1];
                }
            }
            return null;
        };

        final Function<String[], String> custom2Extractor = row -> {
            if (request.getCustom2ColumnIndex() != null) {
                final int custom2ColumnIndex = request.getCustom2ColumnIndex();
                if (custom2ColumnIndex > 0 && custom2ColumnIndex <= row.length) {
                    return row[custom2ColumnIndex - 1];
                }
            }
            return null;
        };

        final Function<String[], String> custom3Extractor = row -> {
            if (request.getCustom3ColumnIndex() != null) {
                final int custom3ColumnIndex = request.getCustom3ColumnIndex();
                if (custom3ColumnIndex > 0 && custom3ColumnIndex <= row.length) {
                    return row[custom3ColumnIndex - 1];
                }
            }
            return null;
        };

        try (CSVReader csvReader = new CSVReader(new BufferedReader(new InputStreamReader(devicesListContent)))) {

            csvReader.iterator().forEachRemaining(row -> {
                if (row.length == 1 && row[0].trim().isEmpty()) {
                    return;
                }

                final String deviceNumber = deviceNumberExtractor.apply(row);
                final String imei = imeiExtractor.apply(row);
                final String phoneNumber = phoneNumberExtractor.apply(row);
                final String description = descriptionExtractor.apply(row);
                final String custom1 = custom1Extractor.apply(row);
                final String custom2 = custom2Extractor.apply(row);
                final String custom3 = custom3Extractor.apply(row);

                if (deviceNumber != null) {
                    DeviceImportStatusItem item = new DeviceImportStatusItem();
                    item.setDeviceNumber(deviceNumber);
                    item.setImei(imei);
                    item.setPhoneNumber(phoneNumber);
                    item.setDescription(description);
                    item.setCustom1(custom1);
                    item.setCustom2(custom2);
                    item.setCustom3(custom3);

                    try {
                        final Device device = this.deviceDAO.getDeviceByNumberIgnoreCase(deviceNumber);
                        if (device != null) {
                            item.setExistingDeviceId(device.getId());
                        }

                        final String deviceNumberLowerCase = deviceNumber.toLowerCase();
                        if (!deviceNumberCounts.containsKey(deviceNumberLowerCase)) {
                            deviceNumberCounts.put(deviceNumberLowerCase, new AtomicInteger(0));
                        }

                        item.setCount(deviceNumberCounts.get(deviceNumberLowerCase).get());

                        deviceNumberCounts.get(deviceNumberLowerCase).incrementAndGet();

                        items.add(item);

                    } catch (SecurityException e) {
                        logger.error("Device {} belongs to a different customer", deviceNumber);
                    } catch (Exception e) {
                        logger.error("Skipping device {} due to unexpected error", deviceNumber, e);
                    }
                }

            });
        }

        return items;
    }
}
