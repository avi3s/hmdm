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

package com.hmdm.rest.resource;

import javax.inject.Inject;
import javax.inject.Singleton;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import javax.inject.Named;

import com.hmdm.notification.PushService;
import com.hmdm.persistence.ApplicationReferenceExistsException;
import com.hmdm.persistence.ApplicationVersionPackageMismatchException;
import com.hmdm.persistence.CommonAppAccessException;
import com.hmdm.persistence.RecentApplicationVersionExistsException;
import com.hmdm.persistence.domain.ApplicationVersion;
import com.hmdm.rest.json.*;
import com.hmdm.security.SecurityException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hmdm.persistence.ApplicationDAO;
import com.hmdm.persistence.DuplicateApplicationException;
import com.hmdm.persistence.domain.Application;
import com.hmdm.util.FileExistsException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = {"Application"}, authorizations = {@Authorization("Bearer Token")})
@Singleton
@Path("/private/applications")
public class ApplicationResource {

    // A logging service
    private static final Logger logger  = LoggerFactory.getLogger(ApplicationResource.class);
    private File baseDirectory;
    private ApplicationDAO applicationDAO;
    private PushService pushService;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public ApplicationResource() {
    }

    @Inject
    public ApplicationResource(ApplicationDAO applicationDAO,
                               PushService pushService,
                               @Named("files.directory") String filesDirectory) {
        this.applicationDAO = applicationDAO;
        this.pushService = pushService;
        this.baseDirectory = new File(filesDirectory);

        if (!this.baseDirectory.exists()) {
            this.baseDirectory.mkdirs();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get all applications",
            notes = "Gets the list of all available applications",
            response = Application.class,
            responseContainer = "List"
    )
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllApplications() {
        return Response.OK(this.applicationDAO.getAllApplications());
    }

 // =================================================================================================================
    @ApiOperation(
            value = "Search applications",
            notes = "Search applications meeting the specified filter value",
            response = Application.class,
            responseContainer = "List"
    )
    @GET
    @Path("/search/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchApplications(@PathParam("value") @ApiParam("A filter value") String value) {
        return Response.OK(this.applicationDAO.getAllApplicationsByValue(value));
    }

    // =================================================================================================================

    /**
     * <p>Gets the list of application ids/names matching the specified filter for autocompletions.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a response with list of devices matching the specified filter.
     */
    @ApiOperation(value = "Get app ids and names for autocompletions")
    @POST
    @Path("/autocomplete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApplicationsForAutocomplete(String filter) {
        try {
            List<LookupItem> applications
                    = this.applicationDAO.getApplicationPkgLookup(filter, 10);
            return Response.OK(applications);
        } catch (Exception e) {
            logger.error("Failed to search the applications due to unexpected error. Filter: {}", filter, e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get application versions",
            notes = "Gets the list of versions for specified application",
            response = ApplicationVersion.class,
            responseContainer = "List"
    )
    @GET
    @Path("/{id}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllApplicationVersions(@PathParam("id") @ApiParam("Application ID") Integer id) {
        try {
            return Response.OK(this.applicationDAO.getApplicationVersions(id));
        } catch (Exception e) {
            logger.error("Failed to retrieve the versions for application #{}", id, e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get application",
            notes = "Gets the details for specified application",
            response = Application.class
    )
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApplication(@PathParam("id") @ApiParam("Application ID") Integer id) {
        try {
            return Response.OK(this.applicationDAO.findById(id));
        } catch (Exception e) {
            logger.error("Failed to retrieve the details for application #{}", id, e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Create or update Android application",
            notes = "Create a new Android application (if id is not provided) or update existing one otherwise."
    )
    @Path("/android")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateApplication(Application application) {
        try {
            if (application.getId() == null) {
                final int appId = this.applicationDAO.insertApplication(application);
                application = this.applicationDAO.findById(appId);
                return Response.OK(application);
            } else {
                // TODO : ISV : Handle the scenario for inserting new version for the same package here
                this.applicationDAO.updateApplication(application);
                return Response.OK();
            }
        } catch (DuplicateApplicationException e) {
            logger.error("Failed to create or update application", e);
            return Response.DUPLICATE_APPLICATION();
        } catch (RecentApplicationVersionExistsException e) {
            logger.error("Failed to create or update application", e);
            return Response.RECENT_APPLICATION_VERSION_EXISTS();
        } catch (CommonAppAccessException e) {
            logger.error("Failed to create or update application", e);
            return Response.COMMON_APPLICATION_ACCESS_PROHIBITED();
        } catch (FileExistsException e) {
            logger.error("Failed to create or update application", e);
            return Response.FILE_EXISTS();
        } catch (Exception e) {
            logger.error("Failed to create or update application", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Create or update Web-page application",
            notes = "Create a new Web-page application (if id is not provided) or update existing one otherwise."
    )
    @Path("/web")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateWebApplication(Application application) {
        try {
            if (application.getId() == null) {
                final int appId = this.applicationDAO.insertWebApplication(application);
                application = this.applicationDAO.findById(appId);
                return Response.OK(application);
            } else {
                // TODO : ISV : Handle the scenario for inserting new version for the same package here
                this.applicationDAO.updateWebApplication(application);
                return Response.OK();
            }
        } catch (DuplicateApplicationException e) {
            logger.error("Failed to create or update application", e);
            return Response.DUPLICATE_APPLICATION();
        } catch (RecentApplicationVersionExistsException e) {
            logger.error("Failed to create or update application", e);
            return Response.RECENT_APPLICATION_VERSION_EXISTS();
        } catch (CommonAppAccessException e) {
            logger.error("Failed to create or update application", e);
            return Response.COMMON_APPLICATION_ACCESS_PROHIBITED();
        } catch (FileExistsException e) {
            logger.error("Failed to create or update application", e);
            return Response.FILE_EXISTS();
        } catch (Exception e) {
            logger.error("Failed to create or update application", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Create or update application version",
            notes = "Create a new application version (if id is not provided) or update existing one otherwise."
    )
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/versions")
    public Response updateApplicationVersion(ApplicationVersion applicationVersion) {
        try {
            if (applicationVersion.getId() == null) {
                // Here only "url" is coming, we may need to change it to urlArmeabi or urlArm64 if arch is set
                this.applicationDAO.insertApplicationVersion(applicationVersion);
                applicationVersion = this.applicationDAO.findApplicationVersionById(applicationVersion.getId());
                return Response.OK(applicationVersion);
            } else {
                this.applicationDAO.updateApplicationVersion(applicationVersion);
                return Response.OK();
            }
        } catch (DuplicateApplicationException e) {
            logger.error("Failed to create or update application version", e);
            return Response.DUPLICATE_APPLICATION();
        } catch (RecentApplicationVersionExistsException e) {
            logger.error("Failed to create or update application version", e);
            return Response.RECENT_APPLICATION_VERSION_EXISTS();
        } catch (ApplicationVersionPackageMismatchException e) {
            logger.error("Failed to create or update application version", e);

            Map<String, String> args = new HashMap<>();
            args.put("expected", e.getExpectedPackageName());
            args.put("actual", e.getActualPackageName());

            return Response.ERROR("error.application.version.pkg.mismatch", args);
        } catch (CommonAppAccessException e) {
            logger.error("Failed to create or update application version", e);
            return Response.COMMON_APPLICATION_ACCESS_PROHIBITED();
        } catch (FileExistsException e) {
            logger.error("Failed to create or update application version", e);
            return Response.FILE_EXISTS();
        } catch (Exception e) {
            logger.error("Failed to create or update application version", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Delete application",
            notes = "Delete an existing application"
    )
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeApplication(@PathParam("id") @ApiParam("Application ID") Integer id) {
        try {
            this.applicationDAO.removeApplicationById(id);
            return Response.OK();
        } catch (SecurityException e) {
            logger.error("Prohibited to delete application #{} by current user", id, e);
            return Response.PERMISSION_DENIED();
        } catch (ApplicationReferenceExistsException e) {
            logger.error("Prohibited to delete application #{} as it is still referenced in configurations", id, e);
            return Response.APPLICATION_CONFIG_REFERENCE_EXISTS();
        } catch (Exception e) {
            logger.error("Failed to delete application #{} due to unexpected error", id, e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Delete application version",
            notes = "Delete an existing application version"
    )
    @DELETE
    @Path("/versions/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeApplicationVersion(@PathParam("id") @ApiParam("Application Version ID") Integer id) {
        try {
            this.applicationDAO.removeApplicationVersionByIdWithAPKFile(id);
            return Response.OK();
        } catch (SecurityException e) {
            logger.error("Prohibited to delete application version #{} by current user", id, e);
            return Response.PERMISSION_DENIED();
        } catch (ApplicationReferenceExistsException e) {
            logger.error("Prohibited to delete application version #{} as it is still referenced in configurations", id, e);
            return Response.APPLICATION_CONFIG_REFERENCE_EXISTS();
        } catch (Exception e) {
            logger.error("Failed to delete application version #{} due to unexpected error", id, e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get application configurations",
            notes = "Gets the list of configurations using requested application",
            response = ApplicationConfigurationLink.class,
            responseContainer = "List"
    )
    @GET
    @Path("/configurations/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApplicationConfigurations(@PathParam("id") @ApiParam("Application ID") Integer id) {
        return Response.OK(this.applicationDAO.getApplicationConfigurations(id));
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get application version configurations",
            notes = "Gets the list of configurations using requested application version",
            response = ApplicationConfigurationLink.class,
            responseContainer = "List"
    )
    @GET
    @Path("/version/{id}/configurations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApplicationVersionConfigurations(
            @PathParam("id") @ApiParam("Application Version ID") Integer id
    ) {
        try {
            return Response.OK(this.applicationDAO.getApplicationVersionConfigurations(id));
        } catch (Exception e) {
            logger.error("Failed to get list of application version configurations", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Update application configurations",
            notes = "Updates the list of configurations using requested application"
    )
    @POST
    @Path("/configurations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateApplicationConfigurations(LinkConfigurationsToAppRequest request) {
        try {
            this.applicationDAO.updateApplicationConfigurations(request);

            return Response.OK();
        } catch (Exception e) {
            logger.error("Unexpected error when updating application configurations", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Update application version configurations",
            notes = "Updates the list of configurations using requested application version"
    )
    @POST
    @Path("/version/configurations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateApplicationVersionConfigurations(LinkConfigurationsToAppVersionRequest request) {
        try {
            this.applicationDAO.updateApplicationVersionConfigurations(request);
            for (ApplicationVersionConfigurationLink configurationLink : request.getConfigurations()) {
                this.pushService.notifyDevicesOnUpdate(configurationLink.getConfigurationId());
            }

            return Response.OK();
        } catch (Exception e) {
            logger.error("Unexpected error when updating application configurations", e);
            return Response.INTERNAL_ERROR();
        }
    }

    @ApiOperation(value = "", hidden = true)
    @GET
    @Path("/admin/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAdminApplications() {
        return Response.OK(this.applicationDAO.getAllAdminApplications());
    }

    @ApiOperation(value = "", hidden = true)
    @GET
    @Path("/admin/search/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchAdminApplications(@PathParam("value") String value) {
        return Response.OK(this.applicationDAO.getAllAdminApplicationsByValue(value));
    }

    @ApiOperation(value = "", hidden = true)
    @GET
    @Path("/admin/common/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response turnApplicationIntoCommon(@PathParam("id") Integer id) {
        try {
            this.applicationDAO.turnApplicationIntoCommon(id);
            return Response.OK();
        } catch (DuplicateApplicationException e) {
            logger.error("Failed to turn application with ID: {} into common", id, e);
            return Response.DUPLICATE_APPLICATION();
        } catch (Exception e) {
            logger.error("Failed to turn application with ID: {} into common", id, e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Validate application package",
            notes = "Validate the application package ID for uniqueness",
            response = Application.class,
            responseContainer = "List"
    )
    @Path("/validatePkg")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateApplication(Application application) {
        try {
            final List<Application> otherApps = this.applicationDAO.getApplicationsForPackageID(application);
            return Response.OK(otherApps);
        } catch (Exception e) {
            logger.error("Failed to validate application", e);
            return Response.INTERNAL_ERROR();
        }
    }
}
