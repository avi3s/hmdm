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

package com.hmdm.plugins.contacts.rest;

import com.hmdm.persistence.ApplicationDAO;
import com.hmdm.persistence.ApplicationSettingDAO;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.ApplicationSetting;
import com.hmdm.persistence.domain.ApplicationSettingType;
import com.hmdm.persistence.domain.User;
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.plugins.contacts.rest.json.ContactsSettingsData;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

/**
 * <p>A resource to be used for managing the <code>Contacts</code> plugin data for customer account associated
 * with current user.</p>
 *
 * @author isv
 */
@Singleton
@Path("/plugins/contacts")
@Api(tags = {"Contacts plugin"})
public class ContactsResource {

    private static final Logger logger = LoggerFactory.getLogger(ContactsResource.class);

    /**
     * <p>An interface to application settings persistence.</p>
     */
    private ApplicationSettingDAO applicationSettingDAO;

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

    private PluginStatusCache pluginStatusCache;

    private final static String VAR_ACCOUNT_TYPE = "account_type";
    private final static String VAR_HTTP_USERNAME = "http_username";
    private final static String VAR_HTTP_PASSWORD = "http_password";
    private final static String VAR_VCF_URL = "vcf_url";
    private final static String VAR_SYNC_TIMESPAN = "sync_timespan";
    private final static String VAR_WIPE_ALL = "wipe_all";

    private final static int DEFAULT_SYNC_TIMESPAN = 86400;

    private final static String PLUGIN_PACKAGE_ID = "com.hmdm.contacts";


    /**
     * <p>A constructor required by swagger.</p>
     */
    public ContactsResource() {
    }

    /**
     * <p>Constructs new <code>ContactsResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public ContactsResource(ApplicationSettingDAO applicationSettingDAO,
                            ApplicationDAO applicationDAO,
                            UnsecureDAO unsecureDAO,
                            DeviceDAO deviceDAO,
                            PluginStatusCache pluginStatusCache) {
        this.applicationSettingDAO = applicationSettingDAO;
        this.applicationDAO = applicationDAO;
        this.unsecureDAO = unsecureDAO;
        this.deviceDAO = deviceDAO;
        this.pluginStatusCache = pluginStatusCache;
    }

    /**
     * <p>Gets the plugin settings for customer account associated with current user. If there are none found in DB
     * then returns default ones.</p>
     *
     * @return plugin settings for current customer account.
     */
    @ApiOperation(
            value = "Get contacts settings for a given configuration",
            response = ContactsSettingsData.class,
            authorizations = {@Authorization("Bearer Token")}
    )

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContactsSettings(@PathParam("id") Integer id) {
        ContactsSettingsData data = new ContactsSettingsData();
        data.setSyncTimespan(DEFAULT_SYNC_TIMESPAN);
        data.setConfigurationId(id);

        List<ApplicationSetting> settings = applicationSettingDAO.getApplicationSettingsByConfigurationId(id);
        if (settings != null) {
            for (ApplicationSetting setting : settings) {
                if (!setting.getApplicationPkg().equals(PLUGIN_PACKAGE_ID)) {
                    continue;
                }
                if (setting.getName().equals(VAR_ACCOUNT_TYPE)) {
                    data.setAccountType(setting.getValue());
                } else if (setting.getName().equals(VAR_SYNC_TIMESPAN)) {
                    try {
                        data.setSyncTimespan(Integer.parseInt(setting.getValue()));
                    } catch (NumberFormatException e) {
                        data.setSyncTimespan(DEFAULT_SYNC_TIMESPAN);
                    }
                } else if (setting.getName().equals(VAR_WIPE_ALL)) {
                    try {
                        data.setWipeAll(Integer.parseInt(setting.getValue()) != 0);
                    } catch (NumberFormatException e) {
                        data.setWipeAll(false);
                    }
                } else if (setting.getName().equals(VAR_VCF_URL)) {
                    data.setVcfUrl(setting.getValue());
                } else if (setting.getName().equals(VAR_HTTP_USERNAME)) {
                    data.setHttpUsername(setting.getValue());
                } else if (setting.getName().equals(VAR_HTTP_PASSWORD)) {
                    data.setHttpPassword(setting.getValue());
                }
            }
        }

        return Response.OK(data);
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveContactsSettings(ContactsSettingsData data) {

        ApplicationSetting applicationSetting = new ApplicationSetting();
        applicationSetting.setType(ApplicationSettingType.STRING);
        applicationSetting.setLastUpdate(System.currentTimeMillis());

        List<Application> contactPluginApps = applicationDAO.findByPackageId(PLUGIN_PACKAGE_ID);
        if (contactPluginApps.size() == 0) {
            return Response.APPLICATION_NOT_FOUND_ERROR();
        }
        applicationSetting.setApplicationId(contactPluginApps.get(0).getId());

        applicationSetting.setName(VAR_ACCOUNT_TYPE);
        applicationSetting.setValue(data.getAccountType());
        applicationSettingDAO.insertApplicationSetting(data.getConfigurationId(), applicationSetting);

        applicationSetting.setName(VAR_SYNC_TIMESPAN);
        applicationSetting.setValue("" + data.getSyncTimespan());
        applicationSettingDAO.insertApplicationSetting(data.getConfigurationId(), applicationSetting);

        applicationSetting.setName(VAR_WIPE_ALL);
        applicationSetting.setValue(data.isWipeAll() ? "1" : "0");
        applicationSettingDAO.insertApplicationSetting(data.getConfigurationId(), applicationSetting);

        applicationSetting.setName(VAR_VCF_URL);
        applicationSetting.setValue(data.getVcfUrl());
        applicationSettingDAO.insertApplicationSetting(data.getConfigurationId(), applicationSetting);

        applicationSetting.setName(VAR_HTTP_USERNAME);
        applicationSetting.setValue(data.getHttpUsername());
        applicationSettingDAO.insertApplicationSetting(data.getConfigurationId(), applicationSetting);

        applicationSetting.setName(VAR_HTTP_PASSWORD);
        applicationSetting.setValue(data.getHttpPassword());
        applicationSettingDAO.insertApplicationSetting(data.getConfigurationId(), applicationSetting);

        return Response.OK();
    }
}
