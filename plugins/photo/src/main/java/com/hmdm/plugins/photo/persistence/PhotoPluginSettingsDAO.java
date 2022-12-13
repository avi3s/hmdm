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

package com.hmdm.plugins.photo.persistence;

import com.google.inject.Inject;
import javax.inject.Named;

import com.google.inject.Singleton;
import com.hmdm.persistence.AbstractDAO;
import com.hmdm.plugins.photo.persistence.domain.PhotoPluginSettings;
import com.hmdm.plugins.photo.persistence.mapper.PhotoMapper;
import com.hmdm.plugins.photo.rest.json.PhotoPluginDeviceSettings;
import com.hmdm.security.SecurityException;

import java.util.Optional;

/**
 * <p>A DAO for {@link PhotoPluginSettings} domain objects.</p>
 *
 * @author isv
 */
@Singleton
public class PhotoPluginSettingsDAO extends AbstractDAO<PhotoPluginSettings> {

    /**
     * <p>An ORM mapper for domain object type.</p>
     */
    private final PhotoMapper photoMapper;

    /**
     * <p>A flag indicating if <code>Places</code> feature of plugin is enabled or not.</p>
     */
    private final boolean featurePlacesEnabled;

    /**
     * <p>Constructs new <code>PhotoPluginSettingsDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PhotoPluginSettingsDAO(PhotoMapper photoMapper,
                                  @Named("plugin.photo.enable.places") boolean featurePlacesEnabled) {
        this.photoMapper = photoMapper;
        this.featurePlacesEnabled = featurePlacesEnabled;
    }

    /**
     * <p>Gets the plugin settings for the customer account associated with the current user.</p>
     *
     * @return plugin settings for current customer account or <code>null</code> if there are no such settings found.
     */
    public PhotoPluginSettings getPluginDeviceSettings() {
        final PhotoPluginSettings settings = getSingleRecord(this.photoMapper::findPluginSettingsByCustomerId);
        if (settings != null) {
            settings.setFeaturePlacesEnabled(this.featurePlacesEnabled);
        }

        return settings;
    }

    /**
     * <p>Inserts the specified plugin settings applying the current security context.</p>
     *
     * @param settings plugin settings to be inserted.
     */
    public void insertPluginSettings(PhotoPluginSettings settings) {
        insertRecord(settings, this.photoMapper::insertPluginSettings);
    }

    /**
     * <p>Updates the specified plugin settings verifying them against the current security context.</p>
     *
     * @param settings plugin settings to be updated.
     */
    public void updatePluginSettings(PhotoPluginSettings settings) {
        updateRecord(
                settings,
                this.photoMapper::updatePluginSettings,
                s -> SecurityException.onCustomerDataAccessViolation(s.getId(), "pluginPhotoSettings")
        );
    }

    /**
     * <p>Gets the plugin settings for the specified customer account. This method intended only for usage when handling
     * the requests from the devices.</p>
     *
     * @return plugin settings for specified customer account or default settings if there are no such settings found.
     */
    public PhotoPluginDeviceSettings getPluginDeviceSettings(int customerId) {
        final PhotoPluginSettings settings = getPluginSettings(customerId);
        return new PhotoPluginDeviceSettings(settings, this.featurePlacesEnabled);
    }

    public PhotoPluginSettings getPluginSettings(int customerId) {
        return Optional
                .ofNullable(this.photoMapper.findPluginSettingsByCustomerId(customerId))
                .orElse(new PhotoPluginSettings());
    }
}
