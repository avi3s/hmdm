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
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.GroupDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.plugins.devicelocations.persistence.DeviceLocationDAO;
import com.hmdm.plugins.devicelocations.persistence.domain.DeviceLocation;
import com.hmdm.plugins.devicelocations.rest.json.DeviceLocationHistoryExportFilter;
import com.hmdm.plugins.devicelocations.rest.json.DeviceLocationHistoryPreview;
import com.hmdm.plugins.devicelocations.rest.json.DeviceLocationPair;
import com.hmdm.plugins.devicelocations.rest.json.DeviceLocationView;
import com.hmdm.plugins.devicelocations.rest.json.DeviceView;
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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>A resource to be used for managing the <code>Device Locations</code> plugin data for customer account associated
 * with current user.</p>
 *
 * @author isv
 */
@Singleton
@Path("/plugins/devicelocations/devicelocations")
@Api("Plugin - Device Locations - Locations")
public class DeviceLocationsResource {

    /**
     * <p>A logger fpr the events.</p>
     */
    private static final Logger logger = LoggerFactory.getLogger(DeviceLocationsResource.class);

    /**
     * <p>An interface to groups persistence layer.</p>
     */
    private GroupDAO groupDAO;

    /**
     * <p>An interface to device locations persistence layer.</p>
     */
    private DeviceLocationDAO deviceLocationDAO;

    /**
     * <p>An interface to device records persistence.</p>
     */
    private DeviceDAO deviceDAO;

    /**
     * <p>A service used for exporting the device location history records.</p>
     */
    private DeviceLocationsExportService deviceLocationsExportService;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public DeviceLocationsResource() {
    }

    /**
     * <p>Constructs new <code>DeviceLocationsResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceLocationsResource(GroupDAO groupDAO, DeviceLocationDAO deviceLocationDAO, DeviceDAO deviceDAO,
                                   DeviceLocationsExportService deviceLocationsExportService) {
        this.groupDAO = groupDAO;
        this.deviceLocationDAO = deviceLocationDAO;
        this.deviceDAO = deviceDAO;
        this.deviceLocationsExportService = deviceLocationsExportService;
    }

    /**
     * <p>Gets the lookup items for setting up the device locations view.</p>
     *
     * @return a response to client.
     */
    @GET
    @Path("/private/lookup")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFormDetails() {
        try {
            final List<LookupItem> groups = this.groupDAO.getAllGroups()
                    .stream()
                    .map(group -> new LookupItem(group.getId(), group.getName()))
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();

            result.put("groups", groups);

            return Response.OK(result);
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving device locations lookup items from DB", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Searches for devices matching the specified criteria.</p>
     *
     * @param filter   a filter to be used for filtering the devices by imei, phone, etc.
     * @param groupId  an optional ID of a group to filter devices for.
     * @param pageNum  a number of page of data to be returned.
     * @param pageSize a maximum number of records to be returned.
     * @return a portion of device list.
     */
    @GET
    @Path("/private/devices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchDevices(@QueryParam("filter")
                                  @ApiParam("A filter to lookup devices by IMEI, numbers and phone numbers")
                                          String filter,
                                  @QueryParam("groupId")
                                  @ApiParam("An ID of group to find matching devices for")
                                          Integer groupId,
                                  @QueryParam("pageNum")
                                  @ApiParam("Number of the page of data to get")
                                          int pageNum,
                                  @QueryParam("pageSize")
                                  @ApiParam("A maximum number of records to be returned")
                                          int pageSize) {
        try {
            PaginatedData<DeviceView> devices = this.deviceLocationDAO.findDevices(filter, groupId, pageNum, pageSize);
            return Response.OK(devices);
        } catch (Exception e) {
            logger.error("Unexpected error while searching for devices", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Gets most recent device location.</p>
     *
     * @return a latest reported location of device.
     */
    @GET
    @Path("/private/device/{id}/location")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceLatestLocation(@PathParam("id") @ApiParam("An ID of a device") int deviceId) {
        try {
            Optional<DeviceLocation> location = this.deviceLocationDAO.getLatestDeviceLocation(deviceId);
            return location
                    .map(loc -> Response.OK(new DeviceLocationView(loc)))
                    .orElse(Response.ERROR("plugin.devicelocations.error.location.not.found"));
        } catch (Exception e) {
            logger.error("Unexpected error while searching for devices", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Gets most recent device locations.</p>
     *
     * @return a list of latest reported locations for requested devices.
     */
    @GET
    @Path("/private/devices/locations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceLatestLocations(@QueryParam("ids") @ApiParam("The list of device IDs") List<Integer> deviceIds) {
        try {
            final Map<Integer, DeviceView> devicesMap = this.deviceLocationDAO.findDevices(deviceIds)
                    .stream()
                    .collect(Collectors.toMap(DeviceView::getId, device -> device));

            final List<DeviceLocation> locations = this.deviceLocationDAO.getLatestDeviceLocations(deviceIds);
            final List<DeviceLocationPair> result = locations.stream()
                    .map(deviceLocation -> {
                        final DeviceView device = devicesMap.get(deviceLocation.getDeviceId());
                        return new DeviceLocationPair(device, new DeviceLocationView(deviceLocation));
                    })
                    .collect(Collectors.toList());

            return Response.OK(result);
        } catch (Exception e) {
            logger.error("Unexpected error while searching for devices", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Gets the preview of the result of querying for device location history.</p>
     *
     * @return the details for the results of the query for device location history.
     */
    @GET
    @Path("/private/device/{id}/history/preview")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceLocationsHistoryPreview(
            @PathParam("id")
            @ApiParam(value = "An ID of device", required = true)
                    Integer deviceId,
            @QueryParam("dateFrom")
            @ApiParam("Lower bound of date range")
                    Long dateFrom,
            @QueryParam("dateTo")
            @ApiParam("Upper bound of date range")
                    Long dateTo
    ) {
        try {
            DeviceLocationHistoryPreview result
                    = this.deviceLocationDAO.getDeviceLocationsHistoryPreview(deviceId, dateFrom, dateTo);
            return Response.OK(result);
        } catch (Exception e) {
            logger.error("Unexpected error while searching for device history records", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Gets the location history records for the specified device.</p>
     *
     * @return a list of latest reported locations for requested devices.
     */
    @GET
    @Path("/private/device/{id}/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceLocationsHistory(
            @PathParam("id")
            @ApiParam(value = "An ID of device", required = true)
                    Integer deviceId,
            @QueryParam("dateFrom")
            @ApiParam("Lower bound of date range")
                    Long dateFrom,
            @QueryParam("dateTo")
            @ApiParam("Upper bound of date range")
                    Long dateTo,
            @QueryParam("pageNum")
            @ApiParam("A number of page of data to return")
                    int pageNum
    ) {
        try {
            List<DeviceLocation> history
                    = this.deviceLocationDAO.getDeviceLocationsHistory(deviceId, dateFrom, dateTo, pageNum);
            final List<DeviceLocationView> result
                    = history.stream().map(DeviceLocationView::new).collect(Collectors.toList());
            return Response.OK(result);
        } catch (Exception e) {
            logger.error("Unexpected error while searching for device history records", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Exports the devices location history records to CSV file and sends it back to client.</p>
     *
     * @return a response to be sent to client.
     */
    @GET
    @Path("/private/device/{id}/history/export")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public javax.ws.rs.core.Response exportDevices(@PathParam("id")
                                                       @ApiParam(value = "An ID of device", required = true)
                                                               Integer deviceId,
                                                   @QueryParam("dateFrom")
                                                       @ApiParam("Lower bound of date range")
                                                               Long dateFrom,
                                                   @QueryParam("dateTo")
                                                       @ApiParam("Upper bound of date range")
                                                               Long dateTo,
                                                   @QueryParam("locale")
                                                       @ApiParam("A locale")
                                                               String locale

    ) {

        DeviceLocationHistoryExportFilter request = new DeviceLocationHistoryExportFilter();
        request.setLocale(locale);
        request.setDeviceId(deviceId);
        request.setDateFrom(dateFrom);
        request.setDateTo(dateTo);

        logger.debug("Export device dynamic info request: {}", request);
        try {
            if (!SecurityContext.get().hasPermission("plugin_devicelocations_access")) {
                if (SecurityContext.get().getCurrentUser().isPresent()) {
                    logger.error("Forbidding access to Device Locations History for user: {}",
                            SecurityContext.get().getCurrentUser().get().getLogin());
                } else {
                    logger.error("Forbidding access to Device Locations History for anonymous user");
                }
                return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.FORBIDDEN).build();
            }

            Device dbDevice = this.deviceDAO.getDeviceById(request.getDeviceId());
            if (dbDevice == null) {
                logger.error("Device {} was not found", request.getDeviceId());
                return javax.ws.rs.core.Response.serverError().build();
            }

            String fileName = dbDevice.getNumber();
            ContentDisposition contentDisposition = ContentDisposition.type("attachment")
                    .fileName(fileName + ".csv")
                    .creationDate(new Date())
                    .build();
            return javax.ws.rs.core.Response.ok( (StreamingOutput) output -> {
                try {
                    this.deviceLocationsExportService.exportDeviceLocationHistory(request, output);
                    output.flush();
                } catch ( Exception e ) {
                    logger.error("Unexpected error when exporting the device location history to CSV format", e);
                }
            } ).header( "Content-Disposition", contentDisposition ).build();
        } catch (Exception e) {
            logger.error("Unexpected error while exporting the device location history records to CSV file", e);
            return javax.ws.rs.core.Response.serverError().build();
        }
    }
}
