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

package com.hmdm.plugins.devicereset.rest.json;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.domain.Device;
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.plugins.devicereset.persistence.DeviceResetDAO;
import com.hmdm.plugins.devicereset.persistence.domain.DeviceResetStatus;
import com.hmdm.rest.json.DeviceListHook;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hmdm.plugins.devicereset.DeviceResetPluginConfigurationImpl.PLUGIN_ID;

/**
 * <p>A custom hook for devices list which is responsible to alter the device status code based on current state of
 * request for device resetting to factory settings.</p>
 *
 * @author isv
 */
@Singleton
public class DeviceResetDeviceListHook implements DeviceListHook {

    /**
     * <p>An interface to persistence layer.</p>
     */
    private final DeviceResetDAO deviceResetDAO;

    private final PluginStatusCache pluginStatusCache;

    /**
     * <p>Constructs new <code>DeviceResetSyncResponseHook</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceResetDeviceListHook(DeviceResetDAO deviceResetDAO, PluginStatusCache pluginStatusCache) {
        this.deviceResetDAO = deviceResetDAO;
        this.pluginStatusCache = pluginStatusCache;
    }

    /**
     * <p>Performs the logic specific to this hook. Updates the <code>statusCode</code> of the devices based on the
     * current status of the request for resetting the devices to factory settings (if any).</p>
     *
     * @param original an original list of devices to be handled by this hook.
     * @return a list of devices to be used further in process.
     */
    @Override
    public List<Device> handle(List<Device> original) {
        if (this.pluginStatusCache.isPluginDisabled(PLUGIN_ID)) {
            return original;
        }

        if (original != null) {
            final List<Integer> deviceIds = original.stream().map(Device::getId).collect(Collectors.toList());
            final Map<Integer, DeviceResetStatus> statusMap = this.deviceResetDAO.getDeviceResetStatuses(deviceIds)
                    .stream()
                    .collect(Collectors.toMap(DeviceResetStatus::getDeviceId, s -> s));

            original.forEach(device -> {
                Integer deviceId = device.getId();
                if (statusMap.containsKey(deviceId)) {
                    final DeviceResetStatus status = statusMap.get(deviceId);
                    if (status.getStatusResetConfirmed() != null) {
                        if (status.getStatusResetConfirmed().compareTo(device.getLastUpdate()) > 0) {
                            device.setStatusCode("grey");
                            device.setLastUpdate(status.getStatusResetConfirmed());
                        }
                    } else if (status.isDeviceLocked()) {
                        device.setStatusCode("grey");
                    } else if (status.getStatusResetRequested() != null) {
                        device.setStatusCode("brown");
                    }
                }
            });

        }

        return original;
    }
}
