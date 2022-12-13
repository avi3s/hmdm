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

package com.hmdm.plugins.deviceimport.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import com.hmdm.persistence.ConfigurationDAO;
import com.hmdm.persistence.GroupDAO;
import com.hmdm.plugins.deviceimport.rest.json.DeviceImportConfirmRequest;
import com.hmdm.plugins.deviceimport.rest.json.DeviceImportRequest;
import com.hmdm.plugins.deviceimport.rest.json.DeviceImportResult;
import com.hmdm.plugins.deviceimport.rest.json.DeviceListParsingResult;
import com.hmdm.plugins.deviceimport.service.DeviceImportService;
import com.hmdm.plugins.deviceimport.service.DeviceImportServiceException;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.rest.json.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>A resource to be used for supporting the device imports process.</p>
 *
 * @author isv
 */
@Singleton
@Path("/plugins/deviceimport")
public class DeviceImportResource {

    private static final Logger logger = LoggerFactory.getLogger(DeviceImportResource.class);
    private GroupDAO groupDAO;
    private ConfigurationDAO configurationDAO;
    private DeviceImportService deviceImportService;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public DeviceImportResource() {
    }

    /**
     * <p>Constructs new <code>DeviceImportResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceImportResource(GroupDAO groupDAO,
                                ConfigurationDAO configurationDAO,
                                DeviceImportService deviceImportService) {
        this.groupDAO = groupDAO;
        this.configurationDAO = configurationDAO;
        this.deviceImportService = deviceImportService;
    }

    /**
     * <p>Gets the lookup items for setting up the device imponrt process.</p>
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

            final List<LookupItem> configurations = this.configurationDAO.getAllConfigurations()
                    .stream()
                    .map(config -> new LookupItem(config.getId(), config.getName()))
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();

            result.put("groups", groups);
            result.put("configurations", configurations);

            return Response.OK(result);
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving device import lookup items from DB", e);
            return Response.INTERNAL_ERROR();
        }
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
    @Path("/upload")
    public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream,
                               @ApiParam("A file to upload") @FormDataParam("file") FormDataContentDisposition fileDetail) {
        try {
            final Map<String, String> result = this.deviceImportService.onFileUploaded(uploadedInputStream, fileDetail);
            return Response.OK(result);
        } catch (Exception e) {
            logger.error("Unexpected error when handling file upload", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Parses the uploaded file and analyzes the list of extracted devices to be imported into DB.</p>
     *
     * @param request the parameters of device import process.
     * @return a response to be sent to client.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/private/parseFile")
    public Response parseFile(DeviceImportRequest request) {
        try {
            final DeviceListParsingResult result = this.deviceImportService.prepareImport(request);
            return Response.OK(result);
        } catch (DeviceImportServiceException e) {
            logger.error("Unexpected error while analyzing the Excel file", e);
            return Response.ERROR(e.getErrorCode());
        } catch (Exception e) {
            logger.error("Unexpected error while analyzing the Excel file", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Imports the devices from the uploaded and pre-parsed file into DB.</p>
     *
     * @param request the parameters of device import process.
     * @return a response to be sent to client.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/private/import")
    public Response importDevices(DeviceImportConfirmRequest request) {
        try {
            final DeviceImportResult result = this.deviceImportService.importDevices(request);
            return Response.OK(result);
        } catch (DeviceImportServiceException e) {
            logger.error("Unexpected error while importing the device records from Excel file", e);
            return Response.ERROR(e.getErrorCode());
        } catch (Exception e) {
            logger.error("Unexpected error while importing the device records from Excel file", e);
            return Response.INTERNAL_ERROR();
        }
    }

}
