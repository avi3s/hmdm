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
import com.hmdm.persistence.CommonDAO;
import com.hmdm.persistence.domain.Settings;
import com.hmdm.plugins.deviceexport.persistence.domain.DeviceExportApplicationConfigurationView;
import com.hmdm.plugins.deviceexport.persistence.domain.DeviceExportApplicationDeviceView;
import com.hmdm.plugins.deviceexport.persistence.domain.DeviceExportGroupDeviceView;
import com.hmdm.plugins.deviceexport.persistence.domain.DeviceExportRecord;
import com.hmdm.plugins.deviceexport.persistence.mapper.DeviceExportMapper;
import com.hmdm.util.ResourceBundleUTF8Control;
import com.opencsv.CSVWriter;
import org.apache.ibatis.cursor.Cursor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>A writer for exported device data using format of CSV files.</p>
 */
public class DeviceExportCSVWriter implements DeviceExportWriter {

    private CommonDAO commonDAO;

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    /**
     * <p>Constructs new <code>DeviceExportCSVWriter</code> instance. This implementation does nothing.</p>
     */
    public DeviceExportCSVWriter() {
        // Empty
    }

    /**
     * <p>Constructs new <code>DeviceExportCSVWriter</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceExportCSVWriter(CommonDAO commonDAO) {
        this.commonDAO = commonDAO;
    }

    /**
     * <p>Writes the data for the specified exported devices to specified output.</p>
     *
     * @param devices        a cursor over the list of the devices to be exported.
     * @param output         a stream to write the data for the exported devices to.
     * @param locale         a locale to be used for customizing the output.
     * @param configurations a mapping from configuration ID to list of applications set for the configuration.
     * @param devicesApps    a mapping from device ID to list of applications installed on device.
     * @throws IOException if an I/O error occurs while writting device data.
     */
    @Override
    public void exportDevices(Cursor<DeviceExportRecord> devices,
                              OutputStream output,
                              String locale,
                              String[] columns,
                              Map<Integer, List<DeviceExportApplicationConfigurationView>> configurations,
                              Map<Integer, List<DeviceExportApplicationDeviceView>> devicesApps,
                              Map<Integer, List<DeviceExportGroupDeviceView>> devicesGroups) throws IOException {

        if (locale.contains("_")) {
            locale = locale.substring(0, locale.indexOf('_'));
        }

        ResourceBundle translations = ResourceBundle.getBundle(
                "plugin_deviceexport_translations", new Locale(locale), new ResourceBundleUTF8Control()
        );

        Set<String> columnsSet = new HashSet<String>();
        Collections.addAll(columnsSet, columns);

        Settings settings = commonDAO.getSettings();

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(output))) {
            // adding header to csv
            List<String> headerList = new LinkedList<String>();
            if (columnsSet.contains("devicenumber")) {
                headerList.add(translations.getString("device.export.header.devicenumber"));
            }
            if (columnsSet.contains("imei")) {
                headerList.add(translations.getString("device.export.header.imei"));
            }
            if (columnsSet.contains("serial")) {
                headerList.add(translations.getString("device.export.header.serial"));
            }
            if (columnsSet.contains("phone")) {
                headerList.add(translations.getString("device.export.header.phone"));
            }
            if (columnsSet.contains("description")) {
                headerList.add(translations.getString("device.export.header.description"));
            }
            if (columnsSet.contains("group")) {
                headerList.add(translations.getString("device.export.header.group"));
            }
            if (columnsSet.contains("configuration")) {
                headerList.add(translations.getString("device.export.header.configuration"));
            }
            if (columnsSet.contains("launcher.version")) {
                headerList.add(translations.getString("device.export.header.launcher.version"));
            }
            if (columnsSet.contains("permission.status")) {
                headerList.add(translations.getString("device.export.header.permission.status"));
            }
            if (columnsSet.contains("installation.status")) {
                headerList.add(translations.getString("device.export.header.installation.status"));
            }
            if (columnsSet.contains("sync.time")) {
                headerList.add(translations.getString("device.export.header.sync.time"));
            }
            if (columnsSet.contains("model")) {
                headerList.add(translations.getString("device.export.header.model"));
            }
            if (columnsSet.contains("launcher")) {
                headerList.add(translations.getString("device.export.header.launcher"));
            }
            if (columnsSet.contains("mdm.mode")) {
                headerList.add(translations.getString("device.export.header.mdm.mode"));
            }
            if (columnsSet.contains("kiosk.mode")) {
                headerList.add(translations.getString("device.export.header.kiosk.mode"));
            }
            if (columnsSet.contains("android.version")) {
                headerList.add(translations.getString("device.export.header.android.version"));
            }
            if (columnsSet.contains("custom1") && settings.getCustomPropertyName1() != null && !settings.getCustomPropertyName1().trim().equals("")) {
                headerList.add(settings.getCustomPropertyName1());
            }
            if (columnsSet.contains("custom2") && settings.getCustomPropertyName2() != null && !settings.getCustomPropertyName2().trim().equals("")) {
                headerList.add(settings.getCustomPropertyName2());
            }
            if (columnsSet.contains("custom3") && settings.getCustomPropertyName3() != null && !settings.getCustomPropertyName3().trim().equals("")) {
                headerList.add(settings.getCustomPropertyName3());
            }
            String[] headers = new String[headerList.size()];
            headerList.toArray(headers);
            writer.writeNext(headers);

            // Output devices data
            devices.forEach(device -> {
                device.setApplications(devicesApps.get(device.getId()));
                writeDevice(device, translations, columnsSet, configurations, devicesGroups, writer, settings);
            });
        }
    }

    /**
     * <p>Writes the data for specified device as row in CSV file.</p>
     *
     * @param device         a device to be exported.
     * @param translations   a locale to be used for localizing the generated content.
     * @param columnsSet     exported columns
     * @param configurations the details for available configurations.
     * @param writer         a writer for CSV file.
     */
    private void writeDevice(DeviceExportRecord device,
                             ResourceBundle translations,
                             Set<String> columnsSet,
                             Map<Integer, List<DeviceExportApplicationConfigurationView>> configurations,
                             Map<Integer, List<DeviceExportGroupDeviceView>> devicesGroups,
                             CSVWriter writer,
                             Settings settings) {

        final List<String> permissionStatusKeys = encodePermissionsStatus.apply(device);
        StringBuilder permissions = new StringBuilder();
        for (String key : permissionStatusKeys) {
            if (permissions.length() > 0) {
                permissions.append("\n");
            }
            permissions.append(translations.getString(key));
        }
        List<String> deviceDataList = new LinkedList<String>();
        if (columnsSet.contains("devicenumber")) {
            deviceDataList.add(device.getDeviceNumber());
        }
        if (columnsSet.contains("imei")) {
            deviceDataList.add(stripTrailingQuotes.apply(device.getImei()));
        }
        if (columnsSet.contains("serial")) {
            deviceDataList.add(stripTrailingQuotes.apply(device.getSerial()));
        }
        if (columnsSet.contains("phone")) {
            deviceDataList.add(stripTrailingQuotes.apply(device.getPhone()));
        }
        if (columnsSet.contains("description")) {
            deviceDataList.add(stripTrailingQuotes.apply(device.getDescription()));
        }
        if (columnsSet.contains("group")) {
            deviceDataList.add(evaluateDeviceGroups(device, devicesGroups));
        }
        if (columnsSet.contains("configuration")) {
            deviceDataList.add(device.getConfigurationName());
        }
        if (columnsSet.contains("launcher.version")) {
            deviceDataList.add(stripTrailingQuotes.apply(device.getLauncherVersion()));
        }
        if (columnsSet.contains("permission.status")) {
            deviceDataList.add(permissions.toString());
        }
        if (columnsSet.contains("installation.status")) {
            deviceDataList.add(device.isInfoAvailable() ? evaluateDeviceAppInstallationStatus(device, translations, configurations) : "");
        }
        if (columnsSet.contains("sync.time")) {
            if (device.getSyncTime() > 0) {
                deviceDataList.add(dateFormat.format(new Date(device.getSyncTime())));
            } else {
                deviceDataList.add("");
            }
        }
        if (columnsSet.contains("model")) {
            deviceDataList.add(stripTrailingQuotes.apply(device.getModel()));
        }
        if (columnsSet.contains("launcher")) {
            deviceDataList.add(stripTrailingQuotes.apply(device.getLauncher()));
        }
        if (columnsSet.contains("mdm.mode")) {
            deviceDataList.add(device.getMdmMode() == null ? "" : device.getMdmMode().toString());
        }
        if (columnsSet.contains("kiosk.mode")) {
            deviceDataList.add(device.getKioskMode() == null ? "" : device.getKioskMode().toString());
        }
        if (columnsSet.contains("android.version")) {
            deviceDataList.add(stripTrailingQuotes.apply(device.getAndroidVersion()));
        }
        if (columnsSet.contains("custom1") && settings.getCustomPropertyName1() != null && !settings.getCustomPropertyName1().trim().equals("")) {
            deviceDataList.add(stripTrailingQuotes.apply(device.getCustom1()));
        }
        if (columnsSet.contains("custom2") && settings.getCustomPropertyName2() != null && !settings.getCustomPropertyName2().trim().equals("")) {
            deviceDataList.add(stripTrailingQuotes.apply(device.getCustom2()));
        }
        if (columnsSet.contains("custom3") && settings.getCustomPropertyName3() != null && !settings.getCustomPropertyName3().trim().equals("")) {
            deviceDataList.add(stripTrailingQuotes.apply(device.getCustom3()));
        }

        String[] deviceData = new String[deviceDataList.size()];
        deviceDataList.toArray(deviceData);

        writer.writeNext(deviceData, true);
    }
}
