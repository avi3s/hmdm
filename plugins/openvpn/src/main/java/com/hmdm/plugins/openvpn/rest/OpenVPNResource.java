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

package com.hmdm.plugins.openvpn.rest;

import com.hmdm.notification.PushService;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.persistence.*;
import com.hmdm.persistence.domain.*;
import com.hmdm.plugins.openvpn.persistence.OpenVPNPluginDefaultsDAO;
import com.hmdm.plugins.openvpn.persistence.domain.OpenVPNPluginDefaults;
import com.hmdm.plugins.openvpn.rest.json.OpenVPNRunAppData;
import com.hmdm.plugins.openvpn.rest.json.OpenVPNSettingsData;
import com.hmdm.rest.json.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * <p>A resource to be used for managing the <code>OpenVPN</code> plugin data for customer account associated
 * with current user.</p>
 *
 * @author isv
 */
@Singleton
@Path("/plugins/openvpn")
@Api(tags = {"OpenVPN plugin"})
public class OpenVPNResource {

    private static final Logger logger = LoggerFactory.getLogger(OpenVPNResource.class);

    /**
     * <p>An interface to application settings persistence.</p>
     */
    private ApplicationSettingDAO applicationSettingDAO;

    /**
     * <p>An interface to default settings persistence.</p>
     */
    private OpenVPNPluginDefaultsDAO pluginDefaultsDAO;

    /**
     * <p>An interface to application persistence.</p>
     */
    private ApplicationDAO applicationDAO;

    /**
     * <p>An interface to persistence without security checks.</p>
     */
    private UnsecureDAO unsecureDAO;

    /**
     * <p>An interface to device records persistence.</p>
     */
    private DeviceDAO deviceDAO;

    private ConfigurationFileDAO configurationFileDAO;

    private PushService pushService;

    private final static String VAR_REMOVE = "remove";
    private final static String VAR_REMOVE_ALL = "remove_all";
    private final static String VAR_VPN_NAME = "vpn_name";
    private final static String VAR_VPN_CONFIG = "vpn_config";
    private final static String VAR_CONNECT = "connect";
    private final static String VAR_ALWAYS_ON = "always_on";

    private final static String PLUGIN_PACKAGE_ID = "de.blinkt.openvpn";


    /**
     * <p>A constructor required by swagger.</p>
     */
    public OpenVPNResource() {
    }

    /**
     * <p>Constructs new <code>OpenVPNResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public OpenVPNResource(ApplicationSettingDAO applicationSettingDAO,
                           OpenVPNPluginDefaultsDAO pluginDefaultsDAO,
                           ApplicationDAO applicationDAO,
                           UnsecureDAO unsecureDAO,
                           DeviceDAO deviceDAO,
                           ConfigurationFileDAO configurationFileDAO,
                           PushService pushService) {
        this.applicationSettingDAO = applicationSettingDAO;
        this.pluginDefaultsDAO = pluginDefaultsDAO;
        this.applicationDAO = applicationDAO;
        this.unsecureDAO = unsecureDAO;
        this.deviceDAO = deviceDAO;
        this.configurationFileDAO = configurationFileDAO;
        this.pushService = pushService;
    }

    /**
     * <p>Gets the plugin settings for customer account associated with current user. If there are none found in DB
     * then returns default ones.</p>
     *
     * @return plugin settings for current customer account.
     */
    @ApiOperation(
            value = "Get OpenVPN settings for a given configuration",
            response = OpenVPNSettingsData.class,
            authorizations = {@Authorization("Bearer Token")}
    )

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOpenVPNSettings(@PathParam("id") Integer id) {
        OpenVPNSettingsData data = new OpenVPNSettingsData();

        if (id == null || id == 0) {
            // Default settings
            OpenVPNPluginDefaults defaults = pluginDefaultsDAO.getDefaultSettings();
            return Response.OK(new OpenVPNSettingsData(defaults));
        }

        data.setConfigurationId(id);

        List<ApplicationSetting> settings = applicationSettingDAO.getApplicationSettingsByConfigurationId(id);
        if (settings != null) {
            for (ApplicationSetting setting : settings) {
                if (!setting.getApplicationPkg().equals(PLUGIN_PACKAGE_ID)) {
                    continue;
                }
                if (setting.getName().equals(VAR_REMOVE)) {
                    data.setRemoveVpns(setting.getValue());
                } else if (setting.getName().equals(VAR_REMOVE_ALL)) {
                    data.setRemoveAll(setting.getValue().equals("1"));
                } else if (setting.getName().equals(VAR_VPN_NAME)) {
                    data.setVpnName(setting.getValue());
                } else if (setting.getName().equals(VAR_VPN_CONFIG)) {
                    data.setVpnConfig(setting.getValue());
                } else if (setting.getName().equals(VAR_CONNECT)) {
                    data.setConnect(setting.getValue().equals("1"));
                } else if (setting.getName().equals(VAR_ALWAYS_ON)) {
                    data.setAlwaysOn(setting.getValue().equals("1"));
                }
            }
        }

        if (data.getVpnConfig() != null && !data.getVpnConfig().trim().equals("")) {
            // VPN URL is retrieved from the configuration files
            ConfigurationFile file = configurationFileDAO.getConfigurationFileByPath(id, data.getVpnConfig());
            if (file != null) {
                data.setVpnUrl(file.getExternalUrl());
            }
        }

        return Response.OK(data);
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveOpenVPNSettings(OpenVPNSettingsData data) {

        if (data.getConfigurationId() == 0) {
            // Request to save default settings
            OpenVPNPluginDefaults defaultSettings = pluginDefaultsDAO.getDefaultSettings();
            defaultSettings.load(data);
            if (defaultSettings.getId() == null || defaultSettings.getId() == 0) {
                pluginDefaultsDAO.insertDefaultSettings(defaultSettings);
            } else {
                pluginDefaultsDAO.updateDefaultSettings(defaultSettings);
            }
            return Response.OK();
        }

        ApplicationSetting applicationSetting = new ApplicationSetting();
        applicationSetting.setType(ApplicationSettingType.STRING);
        applicationSetting.setLastUpdate(System.currentTimeMillis());

        List<Application> contactPluginApps = applicationDAO.findByPackageId(PLUGIN_PACKAGE_ID);
        if (contactPluginApps.size() == 0) {
            return Response.APPLICATION_NOT_FOUND_ERROR();
        }
        applicationSetting.setApplicationId(contactPluginApps.get(0).getId());

        if (data.getRemoveVpns() != null && !data.getRemoveVpns().trim().equals("")) {
            applicationSetting.setName(VAR_REMOVE);
            applicationSetting.setValue(data.getRemoveVpns());
            applicationSettingDAO.insertApplicationSetting(data.getConfigurationId(), applicationSetting);
        } else {
            applicationSettingDAO.deleteApplicationSetting(data.getConfigurationId(), applicationSetting.getApplicationId(), VAR_REMOVE);
        }

        if (data.getRemoveAll() != null && data.getRemoveAll()) {
            applicationSetting.setName(VAR_REMOVE_ALL);
            applicationSetting.setValue("1");
            applicationSettingDAO.insertApplicationSetting(data.getConfigurationId(), applicationSetting);
        } else {
            applicationSettingDAO.deleteApplicationSetting(data.getConfigurationId(), applicationSetting.getApplicationId(), VAR_REMOVE_ALL);
        }

        applicationSetting.setName(VAR_VPN_NAME);
        applicationSetting.setValue(data.getVpnName());
        applicationSettingDAO.insertApplicationSetting(data.getConfigurationId(), applicationSetting);

        applicationSetting.setName(VAR_VPN_CONFIG);
        applicationSetting.setValue(data.getVpnConfig());
        applicationSettingDAO.insertApplicationSetting(data.getConfigurationId(), applicationSetting);

        if (data.getConnect() != null && data.getConnect()) {
            applicationSetting.setName(VAR_CONNECT);
            applicationSetting.setValue("1");
            applicationSettingDAO.insertApplicationSetting(data.getConfigurationId(), applicationSetting);
        } else {
            applicationSettingDAO.deleteApplicationSetting(data.getConfigurationId(), applicationSetting.getApplicationId(), VAR_CONNECT);
        }

        if (data.getAlwaysOn() != null && data.getAlwaysOn()) {
            applicationSetting.setName(VAR_ALWAYS_ON);
            applicationSetting.setValue("1");
            applicationSettingDAO.insertApplicationSetting(data.getConfigurationId(), applicationSetting);
        } else {
            applicationSettingDAO.deleteApplicationSetting(data.getConfigurationId(), applicationSetting.getApplicationId(), VAR_ALWAYS_ON);
        }

        // VPN URL is stored in the configuration files
        if (data.getVpnConfig() != null && !data.getVpnConfig().trim().equals("")) {
            ConfigurationFile file = configurationFileDAO.getConfigurationFileByPath(data.getConfigurationId(), data.getVpnConfig());
            if (file != null) {
                if (!file.getExternalUrl().equals(data.getVpnUrl())) {
                    file.setExternalUrl(data.getVpnUrl());
                    file.setLastUpdate(System.currentTimeMillis());
                    configurationFileDAO.updateConfigurationFile(file);
                }
            } else {
                file = new ConfigurationFile();
                file.setConfigurationId(data.getConfigurationId());
                file.setDevicePath(data.getVpnConfig());
                file.setExternalUrl(data.getVpnUrl());
                file.setLastUpdate(System.currentTimeMillis());
                file.setReplaceVariables(true);
                configurationFileDAO.insertConfigurationFile(file);
            }
        }

        pushService.notifyDevicesOnUpdate(data.getConfigurationId());

        return Response.OK();
    }

    // Run OpenVPN mobile application
    @PUT
    @Path("/run/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response runOpenVPNApp(OpenVPNRunAppData data) {

        List<Device> devices = new LinkedList<>();

        if (data.getScope().equals("device")) {
            Device device = deviceDAO.getDeviceByNumber(data.getDeviceNumber());
            if (device == null) {
                return Response.DEVICE_NOT_FOUND_ERROR();
            }
            devices.add(device);
        } else if (data.getScope().equals("configuration")) {
            devices = deviceDAO.getDeviceIdsByConfigurationId(data.getConfigurationId());
        } else {
            return Response.INTERNAL_ERROR();
        }

        for (Device device : devices) {
            PushMessage pushMessage = new PushMessage();
            pushMessage.setDeviceId(device.getId());
            pushMessage.setMessageType(PushMessage.TYPE_RUN_APP);
            pushMessage.setPayload("{pkg:\"" + PLUGIN_PACKAGE_ID + "\"}");

            pushService.send(pushMessage);
        }

        return Response.OK();
    }
}
