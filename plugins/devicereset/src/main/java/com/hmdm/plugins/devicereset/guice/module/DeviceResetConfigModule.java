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

package com.hmdm.plugins.devicereset.guice.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.hmdm.plugins.devicereset.rest.json.DeviceResetDeviceListHook;
import com.hmdm.rest.json.DeviceListHook;
import com.hmdm.rest.json.SyncResponseHook;
import com.hmdm.plugins.devicereset.rest.json.DeviceResetSyncResponseHook;

/**
 * <p>A <code>Guice</code> module used for configuring the environment for <code>Device Reset</code> plugin.</p>
 *
 * @author isv
 */
public class DeviceResetConfigModule extends AbstractModule {

    /**
     * <p>Constructs new <code>DeviceResetConfigModule</code> instance. This implementation does nothing.</p>
     */
    public DeviceResetConfigModule() {
    }

    /**
     * <p>Configures the environment.</p>
     */
    @Override
    protected void configure() {
        Multibinder<SyncResponseHook> multibinder
                = Multibinder.newSetBinder(binder(), SyncResponseHook.class);
        multibinder.addBinding().to(DeviceResetSyncResponseHook.class);

        Multibinder<DeviceListHook> multibinder2
                = Multibinder.newSetBinder(binder(), DeviceListHook.class);
        multibinder2.addBinding().to(DeviceResetDeviceListHook.class);
    }
}
