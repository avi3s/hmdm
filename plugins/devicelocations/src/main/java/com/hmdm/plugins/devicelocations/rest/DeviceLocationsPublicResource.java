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

import com.google.inject.Singleton;
import com.hmdm.event.DeviceLocationUpdatedEvent;
import com.hmdm.event.EventService;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.GroupDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.UserDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.domain.User;
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.plugins.devicelocations.DeviceLocationsPluginConfigurationImpl;
import com.hmdm.plugins.devicelocations.persistence.DeviceLocationDAO;
import com.hmdm.plugins.devicelocations.persistence.domain.DeviceLocation;
import com.hmdm.plugins.devicelocations.rest.json.*;
import com.hmdm.plugins.devicelocations.service.DeviceLocationsExportService;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.rest.json.PaginatedData;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>A resource to be used for retrieving <code>Device Locations</code> data from devices</p>
 *
 * @author isv
 */
@Singleton
@Path("/plugins/devicelocations/public")
@Api("Plugin - Device Locations - Public")
public class DeviceLocationsPublicResource {

    private static final Logger logger = LoggerFactory.getLogger(DeviceLocationsPublicResource.class);

    private PluginStatusCache pluginStatusCache;

    private UnsecureDAO unsecureDAO;

    private DeviceLocationDAO deviceLocationDAO;

    private EventService eventService;

    private UserDAO userDAO;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public DeviceLocationsPublicResource() {
    }

    /**
     * <p>Constructs new <code>DeviceLocationsPublicResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceLocationsPublicResource(UnsecureDAO unsecureDAO,
                                         DeviceLocationDAO deviceLocationDAO,
                                         UserDAO userDAO,
                                         EventService eventService,
                                         PluginStatusCache pluginStatusCache) {
        this.unsecureDAO = unsecureDAO;
        this.userDAO = userDAO;
        this.deviceLocationDAO = deviceLocationDAO;
        this.eventService = eventService;
        this.pluginStatusCache = pluginStatusCache;
    }

    /**
     * <p>Registers location data sent by a device.</p>
     */
    @PUT
    @Path("/update/{deviceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerLocationUpdate(@PathParam("deviceId")
                                           @ApiParam("An identifier of device within MDM server")
                                           String deviceNumber,
                                           List<com.hmdm.rest.json.DeviceLocation> locations) {
        try {
            final Device dbDevice = this.unsecureDAO.getDeviceByNumber(deviceNumber);
            if (dbDevice == null) {
                logger.error("Device {} was not found", deviceNumber);
                return Response.DEVICE_NOT_FOUND_ERROR();
            }

            if (locations.size() == 0) {
                logger.info("Empty location list for device {}", deviceNumber);
                return Response.OK();
            }

            User user = userDAO.findOrgAdmin(dbDevice.getCustomerId());
            if (user == null) {
                logger.error("No admin user for customer {}, location history ignored", dbDevice.getCustomerId());
                return Response.ERROR();
            }
            SecurityContext.init(user);
            try {
                if (this.pluginStatusCache.isPluginDisabled(DeviceLocationsPluginConfigurationImpl.PLUGIN_ID)) {
                    logger.error("Rejecting request from device {} due to disabled plugin", deviceNumber);
                    return Response.PLUGIN_DISABLED();
                }

                DeviceLocation lastLocation = deviceLocationDAO.getLatestDeviceLocation(dbDevice.getId()).get();
                com.hmdm.rest.json.DeviceLocation latestLocation = null;
                long latestTs = 0;
                for (com.hmdm.rest.json.DeviceLocation location : locations) {
                    if (latestLocation == null || latestLocation.getTs() < location.getTs()) {
                        latestLocation = location;
                    }
                }
                // Save history
                eventService.fireEvent(new DeviceLocationUpdatedEvent(dbDevice.getId(), locations, true));
                // Save latest location if necessary
                if (latestLocation.getTs() > lastLocation.getTs()) {
                    List<com.hmdm.rest.json.DeviceLocation> latestLocationList = new LinkedList<>();
                    latestLocationList.add(latestLocation);
                    eventService.fireEvent(new DeviceLocationUpdatedEvent(dbDevice.getId(), latestLocationList, false));
                }

                return Response.OK();
            } finally {
                SecurityContext.release();
            }
        } catch (Exception e) {
            logger.error("Unexpected error when registering location updates", e);
            return Response.INTERNAL_ERROR();
        }
    }
}
