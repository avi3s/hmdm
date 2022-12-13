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
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.plugins.photo.persistence.PhotoPluginSettingsDAO;
import com.hmdm.plugins.photo.persistence.PlaceDAO;
import com.hmdm.plugins.photo.persistence.domain.Place;
import com.hmdm.plugins.photo.rest.json.PhotoPluginDeviceSettings;
import com.hmdm.plugins.photo.rest.json.PlaceExcelFileParsingResult;
import com.hmdm.plugins.photo.rest.json.PlaceFilter;
import com.hmdm.plugins.photo.rest.json.PlaceImportConfirmRequest;
import com.hmdm.plugins.photo.rest.json.PlaceImportRequest;
import com.hmdm.plugins.photo.rest.json.PlaceImportResult;
import com.hmdm.plugins.photo.rest.json.PlaceSearchResultItem;
import com.hmdm.plugins.photo.service.PlaceImportService;
import com.hmdm.plugins.photo.service.PlaceImportServiceException;
import com.hmdm.rest.json.PaginatedData;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.hmdm.plugins.photo.PluginConfigurationImpl.PLUGIN_ID;

/**
 * <p>$</p>
 *
 * @author isv
 */
@Api(tags = {"Plugin - Photo - Places"})
@Singleton
@Path("/plugins/photo/place")
public class PlaceResource {

    // A logging service
    private static final Logger logger  = LoggerFactory.getLogger(PlaceResource.class);

    private PlaceImportService placeImportService;

    // An unsecure DAO for getting the data from DB
    private UnsecureDAO unsecureDAO;

    // A DAO for managing the photo plugin settings in DB
    private PhotoPluginSettingsDAO photoPluginSettingsDAO;

    // A DAO for managing the place objects in DB
    private PlaceDAO placeDAO;

    private PluginStatusCache pluginStatusCache;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public PlaceResource() {
    }

    /**
     * <p>Constructs new <code>PlaceResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PlaceResource(PlaceImportService placeImportService,
                         UnsecureDAO unsecureDAO,
                         PhotoPluginSettingsDAO photoPluginSettingsDAO,
                         PlaceDAO placeDAO,
                         PluginStatusCache pluginStatusCache) {
        this.placeImportService = placeImportService;
        this.unsecureDAO = unsecureDAO;
        this.placeDAO = placeDAO;
        this.photoPluginSettingsDAO = photoPluginSettingsDAO;
        this.pluginStatusCache = pluginStatusCache;
    }

    /**
     * <p>Uploads the file to server. Returns a reference to path for uploaded file.</p>
     *
     * @param uploadedInputStream an input stream providing the content of the file to upload.
     * @param fileDetail the details for uploaded file.
     * @return a response to be sent to client.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/private/upload")
    public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream,
                               @ApiParam("A file to upload") @FormDataParam("file") FormDataContentDisposition fileDetail) {
        try {
            final Map<String, String> result = this.placeImportService.onFileUploaded(uploadedInputStream, fileDetail);
            return Response.OK(result);
        } catch (Exception e) {
            logger.error("Unexpected error when handling file upload", e);
            return Response.INTERNAL_ERROR();
        }
    }


    /**
     * <p>Parses the uploaded file and analyzes the list of extracted places to be imported into DB.</p>
     *
     * @param request the parameters of place import process.
     * @return a response to be sent to client.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/private/parsePlacesFile")
    public Response parseFile(PlaceImportRequest request) {
        try {
            final PlaceExcelFileParsingResult result = this.placeImportService.prepareImport(request);
            return Response.OK(result);
        } catch (PlaceImportServiceException e) {
            logger.error("Unexpected error while analyzing the Excel file", e);
            return Response.ERROR(e.getErrorCode());
        } catch (Exception e) {
            logger.error("Unexpected error while analyzing the Excel file", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Imports the places from the uploaded and pre-parsed file into DB.</p>
     *
     * @param request the parameters of place import process.
     * @return a response to be sent to client.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/private/importPlaces")
    public Response importPlaces(PlaceImportConfirmRequest request) {
        try {
            final PlaceImportResult result = this.placeImportService.importPlaces(request);
            return Response.OK(result);
        } catch (PlaceImportServiceException e) {
            logger.error("Unexpected error while importing the place records from Excel file", e);
            return Response.ERROR(e.getErrorCode());
        } catch (Exception e) {
            logger.error("Unexpected error while importing the place records from Excel file", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Gets the content of the thumbnail image for the photo referenced by the ID.</p>
     */
    @ApiOperation(
            value = "Get nearest place",
            notes = "Gets the nearest place around specified coordinates",
            response = PlaceSearchResultItem.class
    )
    @GET
    @Path("/nearest/{deviceNumber}/{lat}/{lng}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response findPlace(
            @PathParam("deviceNumber") String deviceNumber,
            @PathParam("lat") double lat,
            @PathParam("lng") double lng) throws IOException {
        try {
            Device dbDevice = this.unsecureDAO.getDeviceByNumber(deviceNumber);
            if (dbDevice == null) {
                return Response.DEVICE_NOT_FOUND_ERROR();
            }

            SecurityContext.init(dbDevice.getCustomerId());
            try {
                if (this.pluginStatusCache.isPluginDisabled(PLUGIN_ID)) {
                    logger.error("Rejecting request from device {} due to disabled plugin", deviceNumber);
                    return Response.PLUGIN_DISABLED();
                }

                int radius = 500;

                final PhotoPluginDeviceSettings pluginSettings
                        = this.photoPluginSettingsDAO.getPluginDeviceSettings(dbDevice.getCustomerId());
                if (pluginSettings != null) {
                    radius = pluginSettings.getSearchPlaceRadius();
                    if (radius == 0) {
                        radius = Integer.MAX_VALUE;
                    }
                }

                logger.debug("Searching for nearest places around {};{} with radius of {} meters", lat, lng, radius);

                List<PlaceSearchResultItem> places = this.placeDAO.findNearestPlaces(dbDevice.getCustomerId(), lat, lng, radius);
                if (places.isEmpty()) {
                    return Response.ERROR("error.place.not.found");
                } else {
                    return Response.OK(places.get(0));
                }
            } finally {
                SecurityContext.release();
            }
        } catch (Exception e) {
            logger.error("Unexpected error when searching for nearest places", e);
            return Response.INTERNAL_ERROR();
        }
    }
    /**
     * <p>Cancels further processing of the uploaded file.</p>
     *
     * @param request the parameters of place import process.
     * @return a response to be sent to client.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/private/cancelPlacesFile")
    public Response cancelFile(PlaceImportRequest request) {
        try {
            this.placeImportService.cancelFile(request.getFilePathId());
            return Response.OK();
        } catch (PlaceImportServiceException e) {
            logger.error("Unexpected error while canceling the Excel file", e);
            return Response.ERROR(e.getErrorCode());
        } catch (Exception e) {
            logger.error("Unexpected error while canceling the Excel file", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Gets the list of places matching the specified filter.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a response with list of places matching the specified filter.
     */
    @ApiOperation(
            value = "Search places",
            notes = "Gets the list of places matching the specified filter",
            response = PaginatedData.class,
            authorizations = {@Authorization("Bearer Token")}
    )
    @POST
    @Path("/private/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlaces(PlaceFilter filter) {
        try {
            List<Place> places = this.placeDAO.findAll(filter);
            long count = this.placeDAO.countAll(filter);

            return Response.OK(new PaginatedData<>(places, count));
        } catch (Exception e) {
            logger.error("Unexpected error when searching for the places", e);
            return Response.INTERNAL_ERROR();
        }
    }
}
