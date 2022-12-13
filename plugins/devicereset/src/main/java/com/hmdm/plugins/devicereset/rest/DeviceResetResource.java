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

package com.hmdm.plugins.devicereset.rest;

import com.hmdm.notification.PushService;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.domain.User;
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.plugins.devicereset.persistence.DeviceResetDAO;
import com.hmdm.plugins.devicereset.persistence.domain.DeviceResetStatus;
import com.hmdm.plugins.devicereset.rest.json.DeviceLockRequest;
import com.hmdm.plugins.devicereset.rest.json.PasswordResetRequest;
import com.hmdm.rest.json.DeviceLookupItem;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.hmdm.plugins.devicereset.DeviceResetPluginConfigurationImpl.PLUGIN_ID;

/**
 * <p>A resource to be used for managing the <code>Device Reset</code> plugin data for customer account associated
 * with current user.</p>
 *
 * @author isv
 */
@Singleton
@Path("/plugins/devicereset")
@Api(tags = {"Device Reset plugin"})
public class DeviceResetResource {

    private static final Logger logger = LoggerFactory.getLogger(DeviceResetResource.class);

    /**
     * <p>An interface to device reset records persistence.</p>
     */
    private DeviceResetDAO deviceResetDAO;

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

    /**
     * <p>A constructor required by swagger.</p>
     */
    public DeviceResetResource() {
    }

    /**
     * <p>Constructs new <code>DeviceResetResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceResetResource(DeviceResetDAO deviceResetDAO,
                               UnsecureDAO unsecureDAO,
                               DeviceDAO deviceDAO,
                               PushService pushService,
                               PluginStatusCache pluginStatusCache) {
        this.deviceResetDAO = deviceResetDAO;
        this.unsecureDAO = unsecureDAO;
        this.deviceDAO = deviceDAO;
        this.pushService = pushService;
        this.pluginStatusCache = pluginStatusCache;
    }

    // =================================================================================================================

    private interface DeviceAction {
        void performAction(int deviceId, long ts);
    }

    private Response confirmAction(String deviceNumber, String loggedAction, DeviceAction action) {
        try {
            Device dbDevice = this.unsecureDAO.getDeviceByNumber(deviceNumber);
            if (dbDevice == null) {
                logger.error("Device {} was not found", deviceNumber);
                return Response.DEVICE_NOT_FOUND_ERROR();
            }

            SecurityContext.init(dbDevice.getCustomerId());
            try {
                if (this.pluginStatusCache.isPluginDisabled(PLUGIN_ID)) {
                    logger.error("Rejecting {} request from device {} due to disabled plugin", loggedAction, deviceNumber);
                    return Response.PLUGIN_DISABLED();
                }
                action.performAction(dbDevice.getId(), System.currentTimeMillis());
                return Response.OK();
            } finally {
                SecurityContext.release();
            }
        } catch (Exception e) {
            logger.error("Unexpected error when recording device " + loggedAction + " confirmation: ", e);
            return Response.INTERNAL_ERROR();
        }

    }

    // =================================================================================================================
    @ApiOperation(
            value = "Confirm device reset",
            notes = "Records the current time as timestamp of confirming the resetting to factory settings by device"
    )
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/public/{deviceNumber}")
    public Response confirmDeviceReset(@PathParam("deviceNumber") String deviceNumber) {
        logger.debug("confirmDeviceReset => /public/{}", deviceNumber);
        return confirmAction(deviceNumber, "reset", (deviceId, ts) -> {
            this.deviceResetDAO.onDeviceResetConfirmation(deviceId, ts);
        });
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Confirm device reboot",
            notes = "Records the current time as timestamp of confirming the rebooting of the device"
    )
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/public/reboot/{deviceNumber}")
    public Response confirmReboot(@PathParam("deviceNumber") String deviceNumber) {
        logger.debug("confirmReboot => /public/reboot/{}", deviceNumber);
        return confirmAction(deviceNumber, "reboot", (deviceId, ts) -> {
            this.deviceResetDAO.onDeviceRebootConfirmation(deviceId, ts);
        });
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Confirm password reset",
            notes = ""
    )
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/public/password/{deviceNumber}")
    public Response confirmPasswordReset(@PathParam("deviceNumber") String deviceNumber) {
        logger.debug("confirmPasswordReset => /public/password/{}", deviceNumber);
        return confirmAction(deviceNumber, "password", (deviceId, ts) -> {
            this.deviceResetDAO.onPasswordResetConfirmation(deviceId, ts);
        });
    }

    // =================================================================================================================
    private Response checkAccessPermissions(int deviceId) {
        Device dbDevice = this.unsecureDAO.getDeviceById(deviceId);
        if (dbDevice == null) {
            logger.error("Device id {} was not found", deviceId);
            return Response.DEVICE_NOT_FOUND_ERROR();
        }

        if (!SecurityContext.get().hasPermission("plugin_devicereset_access")) {
            if (SecurityContext.get().getCurrentUser().isPresent()) {
                logger.error("Forbidding access to Device Reset for user: {}",
                        SecurityContext.get().getCurrentUser().get().getLogin());
            } else {
                logger.error("Forbidding access to Device Reset for anonymous user");
            }
            return Response.INTERNAL_ERROR();
        }

        User user = SecurityContext.get().getCurrentUser().get();
        if (user.getCustomerId() != dbDevice.getCustomerId()) {
            logger.error("Forbidding access to device {} for user {}", deviceId, user.getLogin());
            return Response.INTERNAL_ERROR();
        }

        return null;
    }

    private Response requestAction(int deviceId, String loggedAction, DeviceAction action) {
        try {
            Response error = checkAccessPermissions(deviceId);
            if (error != null) {
                return error;
            }

            action.performAction(deviceId, System.currentTimeMillis());
            this.pushService.notifyDeviceOnSettingUpdate(deviceId);

            DeviceResetStatus status = this.deviceResetDAO.getDeviceResetStatus(deviceId);
            return Response.OK(status);
        } catch (Exception e) {
            logger.error("Unexpected error when recording device " + loggedAction + " requesting: ",  e);
            return Response.INTERNAL_ERROR();
        }

    }

    // =================================================================================================================
    @ApiOperation(
            value = "Request device reset",
            notes = "Records the current time as timestamp of requesting the resetting to factory settings for device"
    )
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/private/reset/{deviceId}")
    public Response requestDeviceReset(@PathParam("deviceId") int deviceId) {
        logger.debug("requestDeviceReset => /private/reset/{}", deviceId);
        return requestAction(deviceId, "reset", (deviceId1, ts) -> {
            this.deviceResetDAO.onDeviceResetRequest(deviceId1, ts);
        });
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Request device reset",
            notes = "Records the current time as timestamp of requesting the resetting to factory settings for device"
    )
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/private/reboot/{deviceId}")
    public Response requestDeviceReboot(@PathParam("deviceId") int deviceId) {
        logger.debug("requestDeviceReboot => /private/reboot/{}", deviceId);
        return requestAction(deviceId, "reboot", (deviceId1, ts) -> {
            this.deviceResetDAO.onDeviceRebootRequest(deviceId1, ts);
        });
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Toggle device lock",
            notes = "Sets or clears the device lock flag"
    )
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/private/lock")
    public Response requestDeviceLock(DeviceLockRequest request) {
        logger.debug("requestDeviceLock => /private/lock/{}/{}", request.getDeviceId(), request.isLock());
        try {
            Response error = checkAccessPermissions(request.getDeviceId());
            if (error != null) {
                return error;
            }

            this.deviceResetDAO.lockDevice(request.getDeviceId(), request.isLock(), request.getMessage());

            PushMessage pushMessage = new PushMessage();
            pushMessage.setDeviceId(request.getDeviceId());
            pushMessage.setMessageType("configUpdated");

            this.pushService.send(pushMessage);

            DeviceResetStatus status = this.deviceResetDAO.getDeviceResetStatus(request.getDeviceId());
            return Response.OK(status);
        } catch (Exception e) {
            logger.error("Unexpected error when recording device lock requesting: ", e);
            return Response.INTERNAL_ERROR();
        }
    }


    // =================================================================================================================
    @ApiOperation(
            value = "Request the password reset",
            notes = "Sends a new password to the device"
    )
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/private/password")
    public Response requestPasswordReset(PasswordResetRequest request) {
        logger.debug("requestPasswordReset => /private/password/{}/{}", request.getDeviceId(), request.getPassword());
        try {
            Response error = checkAccessPermissions(request.getDeviceId());
            if (error != null) {
                return error;
            }

            this.deviceResetDAO.resetPassword(request.getDeviceId(), request.getPassword());

            PushMessage pushMessage = new PushMessage();
            pushMessage.setDeviceId(request.getDeviceId());
            pushMessage.setMessageType("configUpdated");

            this.pushService.send(pushMessage);

            return Response.OK();
        } catch (Exception e) {
            logger.error("Unexpected error when recording password reset requesting: ", e);
            return Response.INTERNAL_ERROR();
        }
    }


    // =================================================================================================================
    @ApiOperation(
            value = "Search devices",
            notes = "Search ",
            response = DeviceLookupItem.class,
            responseContainer = "List"
    )
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/private/search/device")
    public Response lookupDevices(@QueryParam("filter") String filter, @QueryParam("limit") int limit) {
        try {
            final List<DeviceLookupItem> devices = this.deviceDAO.findDevices(filter, limit);
            return Response.OK(devices);
        } catch (Exception e) {
            logger.error("Unexpected error when retrieving device info", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get device",
            notes = "Gets the details for single device ",
            response = Device.class
    )
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/private/device/{deviceNumber}")
    public Response getDevice(@PathParam("deviceNumber") String deviceNumber) {
        try {
            final Device device = this.deviceDAO.getDeviceByNumber(deviceNumber);
            return Response.OK(device);
        } catch (Exception e) {
            logger.error("Unexpected error when retrieving device info", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get device status",
            notes = "Gets the status for single device ",
            response = Device.class
    )
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/private/status/{deviceNumber}")
    public Response getStatus(@PathParam("deviceNumber") String deviceNumber) {
        try {
            final Device device = this.deviceDAO.getDeviceByNumber(deviceNumber);
            if (device == null) {
                logger.error("Device {} was not found", deviceNumber);
                return Response.DEVICE_NOT_FOUND_ERROR();
            }

            Response error = checkAccessPermissions(device.getId());
            if (error != null) {
                return error;
            }

            DeviceResetStatus status = this.deviceResetDAO.getDeviceResetStatus(device.getId());
            if (status == null) {
                status = new DeviceResetStatus();
                status.setDeviceId(device.getId());
            }
            return Response.OK(status);

        } catch (Exception e) {
            logger.error("Unexpected error when retrieving device reset status", e);
            return Response.INTERNAL_ERROR();
        }
    }
}
