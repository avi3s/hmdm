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
import com.hmdm.util.ResourceBundleUTF8Control;
import org.apache.ibatis.cursor.Cursor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>A writer for exported device data using format of XLSX files.</p>
 */
public class DeviceExportXLSXWriter implements DeviceExportWriter {

    private CommonDAO commonDAO;

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    /**
     * <p>Constructs new <code>DeviceExportXLSXWriter</code> instance. This implementation does nothing.</p>
     */
    public DeviceExportXLSXWriter() {
        // Empty
    }

    /**
     * <p>Constructs new <code>DeviceExportXLSXWriter</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceExportXLSXWriter(CommonDAO commonDAO) {
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
        writeXLSX(devices, output, locale, columns, configurations, devicesApps, devicesGroups);
    }

    /**
     * <p>Exports the specified devices into Excel workbook which is written to specified stream.</p>
     *
     * @param devices        a stream of devices to be exported.
     * @param output         a stream to write the generated content to.
     * @param locale         a locale to be used for localizing the generated content.
     * @param configurations the details for available configurations.
     * @param devicesApps    a mapping from device IDs to list of device applications.
     * @throws IOException if an I/O error occurs.
     */
    private void writeXLSX(Cursor<DeviceExportRecord> devices, OutputStream output, String locale, String[] columns,
                           Map<Integer, List<DeviceExportApplicationConfigurationView>> configurations,
                           Map<Integer, List<DeviceExportApplicationDeviceView>> devicesApps,
                           Map<Integer, List<DeviceExportGroupDeviceView>> devicesGroups) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Devices");

            // Create a header row describing what the columns mean
            CellStyle boldStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            boldStyle.setFont(font);
            boldStyle.setAlignment(HorizontalAlignment.CENTER);

            if (locale.contains("_")) {
                locale = locale.substring(0, locale.indexOf("_"));
            }

            ResourceBundle translations = ResourceBundle.getBundle(
                    "plugin_deviceexport_translations", new Locale(locale), new ResourceBundleUTF8Control()
            );

            Set<String> columnsSet = new HashSet<String>();
            Collections.addAll(columnsSet, columns);

            Settings settings = commonDAO.getSettings();

            Row headerRow = sheet.createRow(0);
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

            addStringCells(headerRow, headerList, boldStyle);

            CellStyle commonStyle = workbook.createCellStyle();
            commonStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            commonStyle.setWrapText(true);

            AtomicInteger rowNum = new AtomicInteger(0);
            devices.forEach(device -> {
                Row row = sheet.createRow(rowNum.incrementAndGet());
                device.setApplications(devicesApps.get(device.getId()));
                addCells(device, row, translations, columnsSet, configurations, devicesGroups, commonStyle, settings);
            });

            int pos = 0;
            if (columnsSet.contains("devicenumber")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("imei")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("serial")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("phone")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("description")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("group")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("configuration")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("launcher.version")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("permission.status")) {
                sheet.setColumnWidth(pos++, 16000);
            }
            if (columnsSet.contains("installation.status")) {
                sheet.setColumnWidth(pos++, 16000);
            }
            if (columnsSet.contains("sync.time")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("model")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("launcher")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("mdm.mode")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("kiosk.mode")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("android.version")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("custom1") && settings.getCustomPropertyName1() != null && !settings.getCustomPropertyName1().trim().equals("")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("custom2") && settings.getCustomPropertyName2() != null && !settings.getCustomPropertyName2().trim().equals("")) {
                sheet.setColumnWidth(pos++, 6000);
            }
            if (columnsSet.contains("custom3") && settings.getCustomPropertyName3() != null && !settings.getCustomPropertyName3().trim().equals("")) {
                sheet.setColumnWidth(pos++, 6000);
            }

            workbook.write(output);
        }
    }

    /**
     * <p>Writes the data for specified device into specified row in Excel workbook.</p>
     *
     * @param device         a device to be exported.
     * @param row            a row in Excel workbook to write the data to.
     * @param translations   a locale to be used for localizing the generated content.
     * @param configurations the details for available configurations.
     * @param commonStyle    a style for applying to all cells.
     */
    private void addCells(DeviceExportRecord device, Row row, ResourceBundle translations, Set<String> columnsSet,
                          Map<Integer, List<DeviceExportApplicationConfigurationView>> configurations,
                          Map<Integer, List<DeviceExportGroupDeviceView>> devicesGroups,
                          CellStyle commonStyle, Settings settings) {
        int pos = 0;

        if (columnsSet.contains("devicenumber")) {
            Cell numberCell = row.createCell(pos++, CellType.STRING);
            numberCell.setCellStyle(commonStyle);
            numberCell.setCellValue(device.getDeviceNumber());
        }

        if (columnsSet.contains("imei")) {
            Cell imeiCell = row.createCell(pos++, CellType.STRING);
            imeiCell.setCellStyle(commonStyle);
            imeiCell.setCellValue(stripTrailingQuotes.apply(device.getImei()));
        }

        if (columnsSet.contains("serial")) {
            Cell serialCell = row.createCell(pos++, CellType.STRING);
            serialCell.setCellStyle(commonStyle);
            serialCell.setCellValue(stripTrailingQuotes.apply(device.getSerial()));
        }

        if (columnsSet.contains("phone")) {
            Cell phoneNumberCell = row.createCell(pos++, CellType.STRING);
            phoneNumberCell.setCellStyle(commonStyle);
            phoneNumberCell.setCellValue(stripTrailingQuotes.apply(device.getPhone()));
        }

        if (columnsSet.contains("description")) {
            Cell descriptionCell = row.createCell(pos++, CellType.STRING);
            descriptionCell.setCellStyle(commonStyle);
            descriptionCell.setCellValue(stripTrailingQuotes.apply(device.getDescription()));
        }

        if (columnsSet.contains("group")) {
            Cell groupCell = row.createCell(pos++, CellType.STRING);
            groupCell.setCellStyle(commonStyle);
            groupCell.setCellValue(evaluateDeviceGroups(device, devicesGroups));
        }

        if (columnsSet.contains("configuration")) {
            Cell configNameCell = row.createCell(pos++, CellType.STRING);
            configNameCell.setCellStyle(commonStyle);
            configNameCell.setCellValue(device.getConfigurationName());
        }

        if (columnsSet.contains("launcher.version")) {
            Cell launcherVersionCell = row.createCell(pos++, CellType.STRING);
            launcherVersionCell.setCellStyle(commonStyle);
            launcherVersionCell.setCellValue(stripTrailingQuotes.apply(device.getLauncherVersion()));
        }

        // Permissions status
        if (columnsSet.contains("permission.status")) {
            final List<String> permissionStatusKeys = encodePermissionsStatus.apply(device);
            StringBuilder b = new StringBuilder();
            for (String key : permissionStatusKeys) {
                if (b.length() > 0) {
                    b.append("\n");
                }
                b.append(translations.getString(key));
            }

            Cell permissionsCell = row.createCell(pos++, CellType.STRING);
            permissionsCell.setCellStyle(commonStyle);
            permissionsCell.setCellValue(b.toString());
        }

        // Installation status
        if (columnsSet.contains("installation.status")) {
            Cell installationsCell = row.createCell(pos++, CellType.STRING);
            installationsCell.setCellStyle(commonStyle);

            if (device.isInfoAvailable()) {
                final String status = evaluateDeviceAppInstallationStatus(device, translations, configurations);
                installationsCell.setCellValue(status);
            }
        }

        if (columnsSet.contains("sync.time")) {
            Cell launcherVersionCell = row.createCell(pos++, CellType.STRING);
            launcherVersionCell.setCellStyle(commonStyle);
            String syncTime = device.getSyncTime() > 0 ? dateFormat.format(new Date(device.getSyncTime())) : "";
            launcherVersionCell.setCellValue(stripTrailingQuotes.apply(syncTime));
        }

        if (columnsSet.contains("model")) {
            Cell launcherVersionCell = row.createCell(pos++, CellType.STRING);
            launcherVersionCell.setCellStyle(commonStyle);
            launcherVersionCell.setCellValue(stripTrailingQuotes.apply(device.getModel()));
        }

        if (columnsSet.contains("launcher")) {
            Cell launcherVersionCell = row.createCell(pos++, CellType.STRING);
            launcherVersionCell.setCellStyle(commonStyle);
            launcherVersionCell.setCellValue(stripTrailingQuotes.apply(device.getLauncher()));
        }

        if (columnsSet.contains("mdm.mode")) {
            Cell launcherVersionCell = row.createCell(pos++, CellType.STRING);
            launcherVersionCell.setCellStyle(commonStyle);
            launcherVersionCell.setCellValue(device.getMdmMode() == null ? "" : device.getMdmMode().toString());
        }

        if (columnsSet.contains("kiosk.mode")) {
            Cell launcherVersionCell = row.createCell(pos++, CellType.STRING);
            launcherVersionCell.setCellStyle(commonStyle);
            launcherVersionCell.setCellValue(device.getKioskMode() == null ? "" : device.getKioskMode().toString());
        }

        if (columnsSet.contains("android.version")) {
            Cell launcherVersionCell = row.createCell(pos++, CellType.STRING);
            launcherVersionCell.setCellStyle(commonStyle);
            launcherVersionCell.setCellValue(stripTrailingQuotes.apply(device.getAndroidVersion()));
        }

        if (columnsSet.contains("custom1") && settings.getCustomPropertyName1() != null && !settings.getCustomPropertyName1().trim().equals("")) {
            Cell customCell1 = row.createCell(pos++, CellType.STRING);
            customCell1.setCellStyle(commonStyle);
            customCell1.setCellValue(device.getCustom1());
        }

        if (columnsSet.contains("custom2") && settings.getCustomPropertyName2() != null && !settings.getCustomPropertyName2().trim().equals("")) {
            Cell customCell2 = row.createCell(pos++, CellType.STRING);
            customCell2.setCellStyle(commonStyle);
            customCell2.setCellValue(device.getCustom2());
        }

        if (columnsSet.contains("custom3") && settings.getCustomPropertyName3() != null && !settings.getCustomPropertyName3().trim().equals("")) {
            Cell customCell3 = row.createCell(pos++, CellType.STRING);
            customCell3.setCellStyle(commonStyle);
            customCell3.setCellValue(device.getCustom3());
        }
    }

    /**
     * <p>Writes the cells with specified content to specified row in Excel workbook.</p>
     *
     * @param row     a row in Excel workbook to write the data to.
     * @param strings the contents of the cells.
     * @param style   a style to be applied to cells.
     */
    private static void addStringCells(Row row, List<String> strings, CellStyle style) {
        for (int i = 0; i < strings.size(); i++) {
            Cell cell = row.createCell(i, CellType.STRING);
            cell.setCellValue(strings.get(i));
            cell.setCellStyle(style);
        }
    }
}
