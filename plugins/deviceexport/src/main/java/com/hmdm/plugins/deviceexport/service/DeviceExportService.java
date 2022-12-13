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

package com.hmdm.plugins.deviceexport.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.plugins.deviceexport.persistence.domain.*;
import com.hmdm.plugins.deviceexport.persistence.mapper.DeviceExportMapper;
import com.hmdm.plugins.deviceexport.rest.json.DeviceExportRequest;
import com.hmdm.security.SecurityContext;
import org.apache.ibatis.cursor.Cursor;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>A service used for supporting device export process.</p>
 *
 * @author isv
 */
@Singleton
public class DeviceExportService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceExportService.class);

    /**
     * <p>An interface to device management services.</p>
     */
    private final DeviceExportMapper mapper;
    private final DeviceExportCSVWriter csvWriter;
    private final DeviceExportXLSXWriter xlsxWriter;

    /**
     * <p>Constructs new <code>DeviceExportService</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceExportService(DeviceExportMapper mapper,
                               DeviceExportCSVWriter csvWriter,
                               DeviceExportXLSXWriter xlsxWriter) {
        this.mapper = mapper;
        this.csvWriter = csvWriter;
        this.xlsxWriter = xlsxWriter;
    }

    /**
     * <p>Exports the devices matching the specified parameters into Excel workbook which is written to specified
     * stream.</p>
     *
     * @param request the parameters for export process.
     * @param output  a stream to write the generated content to.
     * @throws DeviceExportServiceException if an I/O error occurs.
     */
    @Transactional
    public void exportDevices(DeviceExportRequest request, OutputStream output) {
        SecurityContext.get().getCurrentUser().ifPresent(user -> {
            logger.debug("Starting device export for request: {} ...", request);
            request.setCustomerId(user.getCustomerId());
            request.setUserId(user.getId());
            final Map<Integer, List<DeviceExportApplicationConfigurationView>> configurations = this.mapper.getConfigurations(user.getCustomerId())
                    .stream()
                    .collect(Collectors.toMap(
                            DeviceExportConfigurationView::getId, DeviceExportConfigurationView::getApplications)
                    );
            try (Cursor<DeviceExportRecord> devices = this.mapper.getDevicesForExport(request)) {
                if (devices != null) {
                    final Map<Integer, List<DeviceExportApplicationDeviceView>> devicesApps
                            = this.mapper.getDeviceApplicationsByCustomer(user.getCustomerId())
                            .stream()
                            .collect(Collectors.groupingBy(DeviceExportApplicationDeviceView::getDeviceId));

                    final Map<Integer, List<DeviceExportGroupDeviceView>> devicesGroups
                            = this.mapper.getDeviceGroupsByCustomer(user.getCustomerId())
                            .stream()
                            .collect(Collectors.groupingBy(DeviceExportGroupDeviceView::getDeviceId));

                    final DeviceExportWriter exportWriter;
                    switch (request.getExportType()) {
                        case XLSX: {
                            exportWriter = xlsxWriter;
                            break;
                        }
                        case CSV: {
                            exportWriter = csvWriter;
                            break;
                        }
                        default: throw new DeviceExportServiceException("Unsupported export type: " + request.getExportType());
                    }

                    exportWriter.exportDevices(
                            devices,
                            output,
                            request.getLocale() == null ? Locale.ENGLISH.getLanguage() : request.getLocale(),
                            request.getColumns(),
                            configurations,
                            devicesApps,
                            devicesGroups
                    );
                }
            } catch (IOException e) {
                throw new DeviceExportServiceException("Failed to export device records due to I/O error", e);
            } finally {
                logger.debug("Finished device export for request: {}", request);
            }
        });
    }
}
