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
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.rest.json.SyncResponseHook;
import com.hmdm.plugins.devicereset.persistence.DeviceResetDAO;
import com.hmdm.plugins.devicereset.persistence.domain.DeviceResetStatus;
import com.hmdm.rest.json.SyncResponseInt;

import static com.hmdm.plugins.devicereset.DeviceResetPluginConfigurationImpl.PLUGIN_ID;

/**
 * <p>A custom hook for device configuration synchronization response which is responsible to extend the response with
 * the <code>factoryReset </code> property indicating that the device must reset the settings to factory values.</p>
 *
 * @author isv
 */
@Singleton
public class DeviceResetSyncResponseHook implements SyncResponseHook {

    /**
     * <p>An interface to persistence layer.</p>
     */
    private final DeviceResetDAO deviceResetDAO;

    private final PluginStatusCache pluginStatusCache;

    /**
     * <p>Constructs new <code>DeviceResetSyncResponseHook</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceResetSyncResponseHook(DeviceResetDAO deviceResetDAO,
                                       PluginStatusCache pluginStatusCache) {
        this.deviceResetDAO = deviceResetDAO;
        this.pluginStatusCache = pluginStatusCache;
    }

    /**
     * <p>Performs the logic specific to this hook. This hook extends the provided response with
     * <code>factoryReset</code> property if necessary.</p>
     *
     * @param deviceId an ID of a device.
     * @param original an original device configuration synchronization response to be handled by this hook.
     * @return a device configuration synchronization response to be used further in process.
     */
    @Override
    public SyncResponseInt handle(int deviceId, SyncResponseInt original) {
        if (this.pluginStatusCache.isPluginDisabled(PLUGIN_ID)) {
            return original;
        }

        DeviceResetStatus status = this.deviceResetDAO.getDeviceResetStatus(deviceId);

        /**
         * Notice: here is a design bug: only one hook is available - multiple hooks
         * clear information about extra fields of previous hooks
         * Once we have only device reset hook, that's OK, but if you add more hooks
         * this may cause bugs!
         */
        if (status != null) {
            DeviceResetSyncResponse wrapper = new DeviceResetSyncResponse(original);
            if (status.getStatusResetRequested() != null && status.getStatusResetConfirmed() == null) {
                wrapper.setFactoryReset(true);
            }
            if (status.isDeviceLocked()) {
                wrapper.setLock(true);
                wrapper.setLockMessage(status.getLockMessage());
            }
            if (status.getRebootRequested() != null && status.getRebootConfirmed() == null) {
                wrapper.setReboot(true);
            }
            wrapper.setPasswordReset(status.getPassword());
            return wrapper;
        }

        return original;
    }
}
