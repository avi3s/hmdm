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

package com.hmdm.plugins.deviceexport.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import com.hmdm.persistence.ConfigurationDAO;
import com.hmdm.persistence.GroupDAO;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.Group;
import com.hmdm.plugins.deviceexport.rest.json.DeviceExportRequest;
import com.hmdm.plugins.deviceexport.service.DeviceExportService;
import com.hmdm.plugins.deviceexport.service.DeviceExportServiceException;
import com.hmdm.plugins.deviceexport.service.DeviceExportXLSXWriter;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>A resource to be used for supporting the device export process.</p>
 *
 * @author isv
 */
@Singleton
@Path("/plugins/deviceexport")
public class DeviceExportResource {

    /**
     * <p>A logger fpr the events.</p>
     */
    private static final Logger logger = LoggerFactory.getLogger(DeviceExportResource.class);

    private GroupDAO groupDAO;
    private ConfigurationDAO configurationDAO;
    private DeviceExportService deviceExportService;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public DeviceExportResource() {
    }

    /**
     * <p>Constructs new <code>DeviceExportResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceExportResource(GroupDAO groupDAO,
                                ConfigurationDAO configurationDAO,
                                DeviceExportService deviceExportService) {
        this.groupDAO = groupDAO;
        this.configurationDAO = configurationDAO;
        this.deviceExportService = deviceExportService;
    }

    /**
     * <p>Gets the lookup items for setting up the device export process.</p>
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
            logger.error("Unexpected error while retrieving device export lookup items from DB", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Exports the devices related to selected group or configuration to Excel file and sends it back to client.</p>
     *
     * @param request the parameters of device export process.
     * @return a response to be sent to client.
     */
    @POST
    @Path("/private/export")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public javax.ws.rs.core.Response exportDevices(DeviceExportRequest request) {
        logger.debug("Export device request: {}", request);
        try {
            if (!SecurityContext.get().hasPermission("plugin_deviceexport_access")) {
                if (SecurityContext.get().getCurrentUser().isPresent()) {
                    logger.error("Forbidding access to Device Export for user: {}",
                            SecurityContext.get().getCurrentUser().get().getLogin());
                } else {
                    logger.error("Forbidding access to Device Export for anonymous user");
                }
                return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.FORBIDDEN).build();
            }
            request.setUserId(SecurityContext.get().getCurrentUser().get().getId());

            String fileName = "devices";
            if (request.isFilterByGroup() && request.getFilterIds().length == 1 && request.getFilterIds()[0] > 0) {
                final Group group = this.groupDAO.getGroupById(request.getFilterIds()[0]);
                if (group != null) {
                    fileName = group.getName();
                } else {
                    logger.error("Group #{} is not found", request.getFilterIds()[0]);
                    return null;
                }
            } else if (request.isFilterByConfiguration() && request.getFilterIds().length == 1 && request.getFilterIds()[0] > 0) {
                final Configuration config = this.configurationDAO.getConfigurationById(request.getFilterIds()[0]);
                if (config != null) {
                    fileName = config.getName();
                } else {
                    logger.error("Configuration #{} is not found", request.getFilterIds()[0]);
                    return null;
                }
            }

            final String extension;
            switch (request.getExportType()) {
                case XLSX: {
                    extension = ".xlsx";
                    break;
                }
                case CSV: {
                    extension = ".txt";
                    break;
                }
                default: throw new DeviceExportServiceException("Unsupported export type: " + request.getExportType());
            }

            ContentDisposition contentDisposition = ContentDisposition.type("attachment")
                    .fileName(fileName + extension)
                    .creationDate(new Date())
                    .build();
            return javax.ws.rs.core.Response.ok( (StreamingOutput) output -> {
                try {
                    this.deviceExportService.exportDevices(request, output);
                    output.flush();
                } catch ( Exception e ) {
                    logger.error("Unexpected error when exporting the devices to Excel format", e);
                }
            } ).header( "Content-Disposition", contentDisposition ).build();
        } catch (Exception e) {
            logger.error("Unexpected error while exporting the device records to Excel file", e);
            return javax.ws.rs.core.Response.serverError().build();
        }
    }
}
