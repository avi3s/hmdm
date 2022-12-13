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

import com.hmdm.plugins.deviceexport.persistence.domain.DeviceExportApplicationConfigurationView;
import com.hmdm.plugins.deviceexport.persistence.domain.DeviceExportApplicationDeviceView;
import com.hmdm.plugins.deviceexport.persistence.domain.DeviceExportGroupDeviceView;
import com.hmdm.plugins.deviceexport.persistence.domain.DeviceExportRecord;
import com.hmdm.util.ApplicationUtil;
import org.apache.ibatis.cursor.Cursor;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * <p>An interface for the writers for the exported devices.</p>
 */
public interface DeviceExportWriter {

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
    void exportDevices(Cursor<DeviceExportRecord> devices,
                       OutputStream output,
                       String locale,
                       String[] columns,
                       Map<Integer, List<DeviceExportApplicationConfigurationView>> configurations,
                       Map<Integer, List<DeviceExportApplicationDeviceView>> devicesApps,
                       Map<Integer, List<DeviceExportGroupDeviceView>> devicesGroups) throws IOException;


    /**
     * <p>Strips the trailing double quote characters out of specified string. The quotes are stripped off only if
     * specified string starts and ends with double quotes.</p>
     */
    UnaryOperator<String> stripTrailingQuotes = val -> {
        if (val == null) {
            return null;
        } else {
            if (val.startsWith("\"") && val.endsWith("\"")) {
                val = val.substring(1, val.length() - 1);
            }

            return val;
        }
    };

    /**
     * <p>Checks if specified application versions are equal. Removes all non-digit characters from version numbers when
     * analyzing.</p>
     */
    BiPredicate<String, String> areVersionsEqual = (v1, v2) -> {
        String v1d = ApplicationUtil.normalizeVersion(v1);
        String v2d = ApplicationUtil.normalizeVersion(v2);
        return v1d.equals(v2d);

    };

    /**
     * <p>Encodes a permissions status for device. Accepts device as input and returns a key in resource bundle
     * referencing the textual description of permissions status.</p>
     */
    Function<DeviceExportRecord, List<String>> encodePermissionsStatus = device -> {
        final Boolean adminPermission = device.getAdminPermission();
        final Boolean overlapPermission = device.getOverlapPermission();
        final Boolean historyPermission = device.getHistoryPermission();

        final List<String> permissionStatusKeys = new ArrayList<>();
        if (adminPermission == null && overlapPermission == null && historyPermission == null) {
//            permissionStatusKeys.add("devices.unknown");
        } else if (adminPermission != null && overlapPermission != null && historyPermission != null
                && adminPermission && overlapPermission && historyPermission) {
            permissionStatusKeys.add("devices.permissions.all");
        } else {
            if (adminPermission == null || !adminPermission) {
                permissionStatusKeys.add("devices.permissions.not.as.device.admin");
            }
            if (overlapPermission == null || !overlapPermission) {
                permissionStatusKeys.add("devices.permissions.window.overlap.prohibited");
            }
            if (historyPermission == null || !historyPermission) {
                permissionStatusKeys.add("devices.permissions.history.access.prohibited");
            }
        }

        return permissionStatusKeys;
    };

    default String evaluateDeviceGroups(DeviceExportRecord device, Map<Integer, List<DeviceExportGroupDeviceView>> groupsMap) {
        StringBuilder b = new StringBuilder();

        List<DeviceExportGroupDeviceView> groups = groupsMap.get(device.getId());
        if (groups == null) {
            return "";
        }

        groups.stream()
                .forEach(group -> {
                    if (b.length() > 0) {
                        b.append(";");
                    }
                    b.append(group.getGroup());
                });

        return b.toString();
    }

    default String evaluateDeviceAppInstallationStatus(DeviceExportRecord device,
                                                       ResourceBundle translations,
                                                       Map<Integer, List<DeviceExportApplicationConfigurationView>> configurations) {
        StringBuilder b = new StringBuilder();

        final Map<String, DeviceExportApplicationDeviceView> deviceAppsByPkg = device.getApplications() != null ?
                device.getApplications()
                .stream()
                .collect(Collectors.toMap(DeviceExportApplicationDeviceView::getPkg, app -> app, (r1, r2) -> r1)) :
                new HashMap<>();

        if (configurations.containsKey(device.getConfigurationId())) {
            configurations.get(device.getConfigurationId())
                    .stream()
                    .filter(configApp -> configApp.getUrl() != null && !configApp.getUrl().trim().isEmpty())
                    .forEach(configApp -> {

                        final boolean isSystemApp = "0".equals(configApp.getVersion());

                        if (deviceAppsByPkg.containsKey(configApp.getPkg())) {
                            final DeviceExportApplicationDeviceView deviceApp = deviceAppsByPkg.get(configApp.getPkg());
                            if (configApp.getAction() == 2) {
                                if (areVersionsEqual.test(configApp.getVersion(), deviceApp.getVersion())) {
                                    // Needs to be removed on device
                                    if (b.length() > 0) {
                                        b.append("\n");
                                    }

                                    b.append(MessageFormat.format(translations.getString("devices.app.installed"), configApp.getName()));
                                    b.append(MessageFormat.format(translations.getString("devices.app.needs.removal"), deviceApp.getVersion()));
                                }

                            } else if (!isSystemApp && !areVersionsEqual.test(configApp.getVersion(), deviceApp.getVersion())) {
                                // Versions mismatch
                                if (b.length() > 0) {
                                    b.append("\n");
                                }

                                b.append(MessageFormat.format(translations.getString("devices.app.installed.and.version.available"),
                                        configApp.getName(), deviceApp.getVersion(), configApp.getVersion()));
                            }
                        } else if (configApp.getAction() != 2 && configApp.getType().equals("app")) {
                            // Not installed
                            if (b.length() > 0) {
                                b.append("\n");
                            }

                            b.append(MessageFormat.format(translations.getString("devices.app.not.installed"), configApp.getName()));
                            if (!isSystemApp) {
                                b.append(MessageFormat.format(translations.getString("devices.app.version.available"), configApp.getVersion()));
                            }
                        }
                    });
        }

        return b.toString();
    }

}
