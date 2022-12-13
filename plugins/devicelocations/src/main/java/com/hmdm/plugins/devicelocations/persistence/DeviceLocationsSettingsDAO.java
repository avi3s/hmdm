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

package com.hmdm.plugins.devicelocations.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.AbstractDAO;
import com.hmdm.plugins.devicelocations.persistence.domain.DeviceLocationsPluginSettings;
import com.hmdm.plugins.devicelocations.persistence.mapper.DeviceLocationsSettingsMapper;

/**
 * <p>A DAO for {@link DeviceLocationsPluginSettings} domain objects.</p>
 *
 * @author isv
 */
@Singleton
public class DeviceLocationsSettingsDAO extends AbstractDAO<DeviceLocationsPluginSettings> {

    /**
     * <p>An ORM mapper for settings domain object type.</p>
     */
    private final DeviceLocationsSettingsMapper mapper;

    /**
     * <p>Constructs new <code>DeviceLocationsSettingsDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceLocationsSettingsDAO(DeviceLocationsSettingsMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * <p>Gets the plugin settings for the customer account associated with the current user.</p>
     *
     * @return plugin settings for current customer account or <code>null</code> if there are no such settings found.
     */
    public DeviceLocationsPluginSettings getPluginSettings() {
        return getSingleRecord(this.mapper::findPluginSettingsByCustomerId);
    }

    /**
     * <p>Saves the specified plugin settings applying the current security context.</p>
     *
     * @param settings plugin settings to be saved.
     */
    public void savePluginSettings(DeviceLocationsPluginSettings settings) {
        insertRecord(settings, this.mapper::savePluginSettings);
    }
}
