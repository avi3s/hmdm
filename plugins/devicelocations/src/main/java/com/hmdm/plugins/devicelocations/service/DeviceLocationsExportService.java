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

package com.hmdm.plugins.devicelocations.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.plugins.devicelocations.persistence.domain.DeviceLocation;
import com.hmdm.plugins.devicelocations.persistence.mapper.DeviceLocationMapper;
import com.hmdm.plugins.devicelocations.rest.json.DeviceLocationHistoryExportFilter;
import com.hmdm.security.SecurityContext;
import com.hmdm.util.ResourceBundleUTF8Control;
import com.opencsv.CSVWriter;
import org.apache.ibatis.cursor.Cursor;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <p>A service used for supporting device locations export process.</p>
 *
 * @author isv
 */
@Singleton
public class DeviceLocationsExportService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceLocationsExportService.class);

    /**
     * <p>An interface to device locations management services.</p>
     */
    private final DeviceLocationMapper mapper;

    /**
     * <p>Constructs new <code>DeviceLocationsExportService</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceLocationsExportService(DeviceLocationMapper mapper) {
        this.mapper = mapper;
    }


    /**
     * <p>Exports the device dynamic info records matching the specified parameters into CSV file which is written to
     * specified stream.</p>
     *
     * @param request the parameters for export process.
     * @param output  a stream to write the generated content to.
     * @throws DeviceLocationsExportServiceException if an I/O error occurs.
     */
    @Transactional
    public void exportDeviceLocationHistory(DeviceLocationHistoryExportFilter request, OutputStream output) {
        SecurityContext.get().getCurrentUser().ifPresent(user -> {
            logger.debug("Starting device dynamic locations history export for request: {} ...", request);
            try (Cursor<DeviceLocation> records = this.mapper.getDeviceLocationHistoryRecordsForExport(
                    request.getDeviceId(), user.getCustomerId(), request.getDateFrom(), request.getDateTo()
                 );
                 CSVWriter writer = new CSVWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
                if (records != null) {
                    String locale = request.getLocale() == null ? Locale.ENGLISH.getLanguage() : request.getLocale();
                    if (locale.contains("_")) {
                        locale = locale.substring(0, locale.indexOf("_"));
                    }
                    ResourceBundle translations = ResourceBundle.getBundle(
                            "plugin_devicelocations_translations", new Locale(locale), new ResourceBundleUTF8Control()
                    );

                    // adding header to csv
                    final String[] headers = new String[3];
                    headers[0] = translations.getString("plugin.devicelocations.title.time");
                    headers[1] = translations.getString("plugin.devicelocations.title.lat");
                    headers[2] = translations.getString("plugin.devicelocations.title.lon");
                    writer.writeNext(headers);

                    // add data to csv
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    DecimalFormat coordFormat = new DecimalFormat("#########0.0############");
                    records.forEach(record -> {
                        final String[] recordLine = new String[3];
                        recordLine[0] = dateFormat.format(new Date(record.getTs()));
                        recordLine[1] = coordFormat.format(record.getLat());
                        recordLine[2] = coordFormat.format(record.getLon());

                        writer.writeNext(recordLine, false);
                    });
                }
            } catch (IOException e) {
                throw new DeviceLocationsExportServiceException("Failed to export device location history records due to I/O error", e);
            } finally {
                logger.debug("Finished device location history export for request: {}", request);
            }
        });
    }
}
