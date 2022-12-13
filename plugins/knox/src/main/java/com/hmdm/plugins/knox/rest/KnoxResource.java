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

package com.hmdm.plugins.knox.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdm.notification.PushService;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.domain.User;
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.plugins.knox.persistence.KnoxDAO;
import com.hmdm.plugins.knox.persistence.domain.Rule;
import com.hmdm.plugins.knox.rest.json.CopyProfileRequest;
import com.hmdm.plugins.knox.rest.json.KnoxSyncResponse;
import com.hmdm.plugins.knox.rest.json.RuleFilter;
import com.hmdm.rest.json.*;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;
import com.hmdm.util.CryptoUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.hmdm.plugins.knox.KnoxPluginConfigurationImpl.PLUGIN_ID;

/**
 * <p>A resource to be used for managing the <code>Knox</code> plugin data for customer account associated
 * with current user.</p>
 *
 * @author isv
 */
@Singleton
@Path("/plugins/knox")
@Api(tags = {"Knox plugin"})
public class KnoxResource {

    private static final Logger logger = LoggerFactory.getLogger(KnoxResource.class);

    /**
     * <p>An interface to Knox records persistence.</p>
     */
    private KnoxDAO knoxDAO;

    /**
     * <p>An interface to persistence without security checks.</p>
     */
    private UnsecureDAO unsecureDAO;

    /**
     * <p>An interface to device records persistence.</p>
     */
    private DeviceDAO deviceDAO;

    /**
     * <p>An interface to notification services.</p>
     */
    private PushService pushService;

    private PluginStatusCache pluginStatusCache;

    private static final String HEADER_ENROLLMENT_SIGNATURE = "X-Request-Signature";
    private static final String HEADER_RESPONSE_SIGNATURE = "X-Response-Signature";

    private boolean secureEnrollment;
    private String hashSecret;
    private String license;

    /**
     * <p>A constructor required by swagger.</p>
     */
    public KnoxResource() {
    }

    /**
     * <p>Constructs new <code>KnoxResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public KnoxResource(KnoxDAO knoxDAO,
                        UnsecureDAO unsecureDAO,
                        DeviceDAO deviceDAO,
                        PushService pushService,
                        PluginStatusCache pluginStatusCache,
                        @Named("secure.enrollment") boolean secureEnrollment,
                        @Named("hash.secret") String hashSecret,
                        @Named("plugin.knox.license") String license) {
        this.knoxDAO = knoxDAO;
        this.unsecureDAO = unsecureDAO;
        this.deviceDAO = deviceDAO;
        this.pushService = pushService;
        this.pluginStatusCache = pluginStatusCache;
        this.secureEnrollment = secureEnrollment;
        this.hashSecret = hashSecret;
        this.license = license;
    }

    /**
     * <p>Gets the list of Knox rules matching the specified filter.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a response with list of Knox rules matching the specified filter.
     */
    @ApiOperation(
            value = "Search Knox rules",
            notes = "Gets the list of Knox rule records matching the specified filter",
            response = PaginatedData.class,
            authorizations = {@Authorization("Bearer Token")}
    )
    @POST
    @Path("/private/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRules(RuleFilter filter) {
        try {
            List<Rule> records = knoxDAO.findAll(filter);
            long count = knoxDAO.countAll(filter);

            return Response.OK(new PaginatedData<>(records, count));
        } catch (Exception e) {
            logger.error("Failed to search the rule records due to unexpected error. Filter: {}", filter, e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================

    @ApiOperation(
            value = "Create or update Knox filtering rule",
            notes = "Creates a new Knox filtering rule record (if id is not provided) or updates existing one otherwise",
            authorizations = {@Authorization("Bearer Token")}
    )
    @PUT
    @Path("/private/rule")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveRule(Rule rule) {
        try {
            if (rule.getId() == null) {
                knoxDAO.insertRule(rule);
            } else {
                knoxDAO.updateRule(rule);
            }

            return Response.OK();
        } catch (Exception e) {
            logger.error("Failed to create or update device log plugin settings rule", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Delete rule",
            notes = "Delete an existing Knox filtering rule"
    )
    @DELETE
    @Path("/private/rule/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeRule(@PathParam("id") @ApiParam("Rule ID") Integer id) {
        try {
            knoxDAO.deleteRule(id);
            return Response.OK();
        } catch (SecurityException e) {
            logger.error("Prohibited to delete Knox filtering rule #{} by current user", id, e);
            return Response.PERMISSION_DENIED();
        } catch (Exception e) {
            logger.error("Failed to delete  Knox filtering rule #{} due to unexpected error", id, e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    /**
     * <p>Copies the Knox profile of one configuration to another.</p>
     *
     * @param request copy parameters
     */
    @ApiOperation(
            value = "Copy Knox profile",
            notes = "Copies the Knox profile of one configuration to another",
            authorizations = {@Authorization("Bearer Token")}
    )
    @POST
    @Path("/private/copy")
    @Produces(MediaType.APPLICATION_JSON)
    public Response copyProfile(CopyProfileRequest request) {
        try {
            knoxDAO.copyProfile(request.getConfigSrc(), request.getConfigDst(), request.isRemoveExisting());
            return Response.OK();
        } catch (Exception e) {
            logger.error("Failed to copy the Knox profile due to unexpected error. Request: {}", request, e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    /**
     * Let the device retrieve the Knox configuration
     */
    @GET
    @Path("/configuration/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceSetting(@PathParam("deviceId")
                                     @ApiParam("An identifier of device within MDM server")
                                             String number,
                                     @Context HttpServletRequest request,
                                     @Context HttpServletResponse response) {
        logger.debug("/plugin/knox/configuration/{}", number);

        if (secureEnrollment) {
            if (!CryptoUtil.checkRequestSignature(request.getHeader(HEADER_ENROLLMENT_SIGNATURE), hashSecret + number)) {
                return Response.PERMISSION_DENIED();
            }
        }

        try {
            Device dbDevice = this.unsecureDAO.getDeviceByNumber(number);

            if (dbDevice == null) {
                dbDevice = this.unsecureDAO.getDeviceByOldNumber(number);
            }

            // Device creation on demand
            if (dbDevice == null) {
                if (unsecureDAO.isSingleCustomer()) {
                    dbDevice = this.unsecureDAO.createNewDeviceOnDemand(number);
                } else {
                    logger.warn("Not allowed to create devices in the multi-tenant setup");
                }
            }

            if (dbDevice == null) {
                logger.warn("Requested device {} was not found", number);
                return Response.DEVICE_NOT_FOUND_ERROR();
            }

            KnoxSyncResponse data = new KnoxSyncResponse();
            data.setLicense(this.license);
            data.setRules(knoxDAO.getRulesForConfiguration(dbDevice.getConfigurationId()));

            // Always add signature to enable "soft" security implementation
//            if (secureEnrollment) {
                // Add a signature to avoid MITM attack
                response.setHeader(HEADER_RESPONSE_SIGNATURE, CryptoUtil.getDataSignature(hashSecret, data));
//            }

            return Response.OK(data);
        } catch (Exception e) {
            logger.error("Unexpected error when getting Knox device info", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    /**
     * Let the device retrieve the Knox configuration
     * This method uses extra options to create a device on demand
     */
    @POST
    @Path("/configuration/{deviceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceSettingExtended(DeviceCreateOptions createOptions,
                                             @PathParam("deviceId")
                                             @ApiParam("An identifier of device within MDM server")
                                             String number,
                                             @Context HttpServletRequest request,
                                             @Context HttpServletResponse response) {
        logger.debug("/plugin/knox/configuration/{}", number);

        if (secureEnrollment) {
            if (!CryptoUtil.checkRequestSignature(request.getHeader(HEADER_ENROLLMENT_SIGNATURE), hashSecret + number)) {
                return Response.PERMISSION_DENIED();
            }
        }

        try {
            Device dbDevice = this.unsecureDAO.getDeviceByNumber(number);

            if (dbDevice == null) {
                dbDevice = this.unsecureDAO.getDeviceByOldNumber(number);
            }

            // Device creation on demand
            if (dbDevice == null) {
                logger.info("Creating device {} with options {}", number, createOptions.toString());
                dbDevice = this.unsecureDAO.createNewDeviceOnDemand(number, createOptions);
            }

            if (dbDevice == null) {
                logger.warn("Requested device {} was not found", number);
                return Response.DEVICE_NOT_FOUND_ERROR();
            }

            KnoxSyncResponse data = new KnoxSyncResponse();
            data.setLicense(this.license);
            data.setRules(knoxDAO.getRulesForConfiguration(dbDevice.getConfigurationId()));

            // Always add signature to enable "soft" security implementation
//            if (secureEnrollment) {
                // Add a signature to avoid MITM attack
                response.setHeader(HEADER_RESPONSE_SIGNATURE, CryptoUtil.getDataSignature(hashSecret, data));
//            }

            return Response.OK(data);
        } catch (Exception e) {
            logger.error("Unexpected error when getting Knox device info", e);
            return Response.INTERNAL_ERROR();
        }
    }
}
