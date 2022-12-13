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

package com.hmdm.plugins.photo.rest.resource;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hmdm.notification.PushService;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.security.SecurityContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.plugins.photo.persistence.PhotoPluginSettingsDAO;
import com.hmdm.plugins.photo.persistence.domain.PhotoPluginSettings;
import com.hmdm.plugins.photo.rest.json.PhotoPluginDeviceSettings;
import com.hmdm.rest.json.Response;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.List;
import java.util.Optional;

import static com.hmdm.plugins.photo.PluginConfigurationImpl.PLUGIN_ID;

/**
 * <p>A resource to be used for accessing the settings for <code>Photo Plugin</code>.</p>
 *
 * @author isv
 */
@Api(tags = {"Plugin - Photo"})
@Singleton
@Path("/plugins/photo/photo-plugin-settings")
public class PhotoPluginSettingsResource {

    /**
     * <p> Push message type to notify the photo plugin about the configuration update</p>
     */
    private final String PUSH_TYPE_CONFIG_UPDATED = "photoConfigUpdated";

    /**
     * <p>A logger to be used for logging the events.</p>
     */
    private static final Logger log = LoggerFactory.getLogger("plugin-photo");

    /**
     * <p>A DAO for managing the plugin settings.</p>
     */
    private PhotoPluginSettingsDAO settingsDAO;

    /**
     * <p>A DAO for managing the data when handling requests from devices.</p>
     */
    private UnsecureDAO unsecureDAO;

    /**
     * <p>A DAO for getting device list.</p>
     */
    private DeviceDAO deviceDAO;

    /**
     * <p>A DAO for sending Push messages to devices.</p>
     */
    private PushService pushService;

    private PluginStatusCache pluginStatusCache;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public PhotoPluginSettingsResource() {
    }

    /**
     * <p>Constructs new <code>PhotoPluginSettingsResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PhotoPluginSettingsResource(PhotoPluginSettingsDAO settingsDAO,
                                       UnsecureDAO unsecureDAO,
                                       PluginStatusCache pluginStatusCache,
                                       DeviceDAO deviceDAO,
                                       PushService pushService) {
        this.settingsDAO = settingsDAO;
        this.unsecureDAO = unsecureDAO;
        this.pluginStatusCache = pluginStatusCache;
        this.deviceDAO = deviceDAO;
        this.pushService = pushService;
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
            response = PhotoPluginSettings.class,
            authorizations = {@Authorization("Bearer Token")}
    )
    @GET
    @Path("/private")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSettings() {
        PhotoPluginSettings settings = Optional.ofNullable(this.settingsDAO.getPluginDeviceSettings())
                .orElse(new PhotoPluginSettings());
        if (settings.getDirectory() == null) {
            settings.setDirectory(PhotoPluginSettings.DEFAULT_PATH_TEMPLATE.replace('/', File.separatorChar));
        }
        return Response.OK(settings);
    }

    /**
     * <p>Gets the plugin settings for customer account associated with current user. If there are none found in DB
     * then returns default ones.</p>
     *
     * @return plugin settings for current customer account.
     */
    @ApiOperation(
            value = "Create or update plugin settings",
            notes = "Creates a new plugin settings record (if id is not provided) or updates existing one otherwise",
            authorizations = {@Authorization("Bearer Token")}
    )
    @PUT
    @Path("/private")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveSettings(PhotoPluginSettings settings) {
        if (settings.getId() == null) {
            this.settingsDAO.insertPluginSettings(settings);
        } else {
            this.settingsDAO.updatePluginSettings(settings);
        }

        List<Device> devices = deviceDAO.getAllDevices();
        for (Device device : devices) {
            PushMessage message = new PushMessage();
            message.setDeviceId(device.getId());
            message.setMessageType(PUSH_TYPE_CONFIG_UPDATED);
            pushService.send(message);
        }

        return Response.OK();
    }

    /**
     * <p>Gets the plugin settings for customer account associated with specified device. If there are none found in DB
     * then returns default ones.</p>
     * 
     * @param number an unique device number.
     * @return plugin settings for customer account associated with the specified device.
     */
    @ApiOperation(
            value = "Get device settings",
            notes = "Gets the plugin settings for specified device",
            response = PhotoPluginDeviceSettings.class
    )
    @GET
    @Path("/device/{number}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceSetting(@PathParam("number")
                                         @ApiParam("An unique textual identifier of device")
                                                 String number) {
        try {
            Device dbDevice = this.unsecureDAO.getDeviceByNumber(number);
            if (dbDevice != null) {
                SecurityContext.init(dbDevice.getCustomerId());
                try {
                    if (this.pluginStatusCache.isPluginDisabled(PLUGIN_ID)) {
                        log.error("Rejecting request from device {} due to disabled plugin", number);
                        return Response.PLUGIN_DISABLED();
                    }

                    PhotoPluginDeviceSettings pluginSettings = this.settingsDAO.getPluginDeviceSettings(dbDevice.getCustomerId());
                    return Response.OK(pluginSettings);
                } finally {
                    SecurityContext.release();
                }
            } else {
                return Response.DEVICE_NOT_FOUND_ERROR();
            }
        } catch (Exception e) {
            log.error("Unexpected error in /device/{number}", e);
            return Response.INTERNAL_ERROR();
        }
    }
}
