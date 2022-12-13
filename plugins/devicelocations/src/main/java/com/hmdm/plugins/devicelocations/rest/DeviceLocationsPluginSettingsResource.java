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

package com.hmdm.plugins.devicelocations.rest;

import com.hmdm.persistence.ApplicationDAO;
import com.hmdm.persistence.ApplicationSettingDAO;
import com.hmdm.persistence.ConfigurationDAO;
import com.hmdm.persistence.domain.*;
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.plugins.devicelocations.persistence.DeviceLocationsSettingsDAO;
import com.hmdm.plugins.devicelocations.persistence.domain.DeviceLocationsPluginSettings;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

import static com.hmdm.plugins.devicelocations.DeviceLocationsPluginConfigurationImpl.PLUGIN_ID;

/**
 * <p>A resource to be used for managing the <code>Device Locations</code> plugin settings for customer account
 * associated with current user.</p>
 *
 * @author isv
 */
@Singleton
@Path("/plugins/devicelocations/devicelocations-plugin-settings")
@Api(tags = {"Plugin - Device Locations - Settings"})
public class DeviceLocationsPluginSettingsResource {

    /**
     * <p>A logger used for logging the encountered events.</p>
     */
    private static final Logger logger = LoggerFactory.getLogger(DeviceLocationsPluginSettingsResource.class);

    /**
     * <p>An interface to persistence layer.</p>
     */
    private DeviceLocationsSettingsDAO settingsDAO;

    private ApplicationDAO applicationDAO;

    private ApplicationSettingDAO applicationSettingDAO;

    private ConfigurationDAO configurationDAO;

    /**
     * <p>The current status of installed plugins.</p>
     */
    private PluginStatusCache pluginStatusCache;

    // Data keys
    public static final String KEY_UPDATE_TIME = "location_update_time";
    public static final String APPLICATION_PKG = Application.DEFAULT_LAUNCHER_PACKAGE;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public DeviceLocationsPluginSettingsResource() {
    }

    /**
     * <p>Constructs new <code>DeviceLocationsPluginSettingsResource</code> instance. This implementation does nothing.
     * </p>
     */
    @Inject
    public DeviceLocationsPluginSettingsResource(DeviceLocationsSettingsDAO settingsDAO,
                                                 ConfigurationDAO configurationDAO,
                                                 ApplicationDAO applicationDAO,
                                                 ApplicationSettingDAO applicationSettingDAO,
                                                 PluginStatusCache pluginStatusCache) {
        this.settingsDAO = settingsDAO;
        this.configurationDAO = configurationDAO;
        this.applicationDAO = applicationDAO;
        this.applicationSettingDAO = applicationSettingDAO;
        this.pluginStatusCache = pluginStatusCache;
    }

    /**
     * <p>Gets the plugin settings for customer account associated with current user. If there are none found in DB
     * then returns default ones.</p>
     *
     * @return plugin settings for current customer account.
     */
    @ApiOperation(
            value = "Get settings",
            notes = "Gets the plugin settings for current user. If there are none found in DB then returns default ones.",
            response = DeviceLocationsPluginSettings.class,
            authorizations = {@Authorization("Bearer Token")}
    )
    @GET
    @Path("/private")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSettings() {
        try {
            return Response.OK(
                    Optional.ofNullable(this.settingsDAO.getPluginSettings())
                            .orElse(new DeviceLocationsPluginSettings())
            );
        } catch (Exception e) {
            logger.error("Unexpected error when retrieving Device Locations plugin settings", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Save settings",
            notes = "Save the Device Locations plugin settings",
            response = Settings.class
    )
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/private")
    public Response saveSettings(DeviceLocationsPluginSettings settings) {
        try {
            final SecurityContext securityContext = SecurityContext.get();
            if (!securityContext.hasPermission("plugin_devicelocations_settings_access")) {
                securityContext.getCurrentUser().ifPresent(u -> logger.error(
                        "Rejecting access to #saveSettings resource due to unauthorized access by user: {}",
                        u.getLogin())
                );
                return Response.PERMISSION_DENIED();
            }

            DeviceLocationsPluginSettings legacySettings = settingsDAO.getPluginSettings();
            this.settingsDAO.savePluginSettings(settings);
            if ((legacySettings == null && settings.getUpdateTime() > 0) ||
                    legacySettings.getUpdateTime() != settings.getUpdateTime()) {
                updateConfigAppSettings(legacySettings, settings);
            }
            return Response.OK();
        } catch (Exception e) {
            logger.error("Unexpected error when saving Device Locations plugin settings", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // This method checks the application settings of each configuration
    // and if the setting is equal to the legacy setting, replace it with the new value
    // Value=0 is treated as the absence of setting
    private void updateConfigAppSettings(DeviceLocationsPluginSettings legacySettings, DeviceLocationsPluginSettings settings) {
        List<Application> launcherApps = applicationDAO.findByPackageId(APPLICATION_PKG);
        if (launcherApps.size() == 0) {
            logger.error("Can't find the launcher among the applications!");
            return;
        }
        ApplicationSetting setting = new ApplicationSetting();
        setting.setType(ApplicationSettingType.STRING);
        setting.setLastUpdate(System.currentTimeMillis());
        setting.setApplicationId(launcherApps.get(0).getId());
        setting.setName(KEY_UPDATE_TIME);
        setting.setValue("" + settings.getUpdateTime());

        List<Configuration> configurations = configurationDAO.getAllConfigurations();
        for (Configuration configuration : configurations) {
            ApplicationSetting legacySetting = applicationSettingDAO.getApplicationSetting(
                    configuration.getId(), APPLICATION_PKG, KEY_UPDATE_TIME);
            // Update configuration values equal to the legacy value only
            // Other values (manually set) remain unchanged
            if (legacySetting == null) {
                if (settings.getUpdateTime() != 0) {
                    applicationSettingDAO.insertApplicationSetting(configuration.getId(), setting);
                }
            } else {
                int legacyValue = 0;
                try {
                    legacyValue = Integer.parseInt(legacySetting.getValue());
                } catch (Exception e) {
                }
                if (legacyValue == 0 || legacyValue == legacySettings.getUpdateTime()) {
                    if (settings.getUpdateTime() != 0) {
                        applicationSettingDAO.insertApplicationSetting(configuration.getId(), setting);
                    } else {
                        applicationSettingDAO.deleteApplicationSetting(configuration.getId(),
                                legacySetting.getApplicationId(), KEY_UPDATE_TIME);
                    }
                }
            }
        }
    }
}
