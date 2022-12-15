package com.hmdm.rest.resource;

import com.hmdm.persistence.AdminDAO;
import com.hmdm.persistence.domain.admin.*;
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

@Api(tags = {"Admin"}, authorizations = {@Authorization("Bearer Token")})
@Singleton
@Path("/private")
public class AdminResource {

    private static final Logger log = LoggerFactory.getLogger(AdminResource.class);

    private AdminDAO adminDAO;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public AdminResource() {
    }

    @Inject
    public AdminResource(AdminDAO adminDAO) {
        this.adminDAO = adminDAO;
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get Dashboard",
            notes = "Gets the Dashboard Values",
            response = Dashboard.class
    )
    @POST
    @Path("/dashboard")
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getDashboard(Input input) {
        try {
            return Response.OK(null);
        } catch (Exception e) {
            log.error("Unexpected error when getting the dashboard for customer", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get Mandal List w.r.t District Selected in Dashboard",
            notes = "Gets the Mandal List Values",
            response = Mandal.class
    )
    @POST
    @Path("/mandal-details")
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getMandalDetailsLists(Input input) {
        try {
            return Response.OK(null);
        } catch (Exception e) {
            log.error("Unexpected error when getting the Mandal List w.r.t District Selected in Dashboard for current user", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get RBK List w.r.t Mandal Selected in Dashboard",
            notes = "Gets the RBK List Values",
            response = RBK.class
    )
    @POST
    @Path("/rbk")
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getRBKDetailsLists(Input input) {
        try {
            return Response.OK(null);
        } catch (Exception e) {
            log.error("Unexpected error when getting the RBK List w.r.t Mandal Selected in Dashboard for current user", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get Report",
            notes = "Gets the Report Values",
            response = Report.class
    )
    @POST
    @Path("/report")
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getReports(Input input) {
        try {
            return Response.OK(null);
        } catch (Exception e) {
            log.error("Unexpected error when getting the reports for current user", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get Red List",
            notes = "Gets the Red List Values",
            response = RedList.class
    )
    @GET
    @Path("/redList")
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getRedLists() {
        try {
            return Response.OK(null);
        } catch (Exception e) {
            log.error("Unexpected error when getting the red list for current user", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get RBK Details",
            notes = "Gets RBK Details w.r.t RBK-ID",
            response = RBKDetails.class
    )
    @GET
    @Path("/rbk-details/{rbkId}")
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getRBKDetails(@PathParam("rbkId") String rbkId) {
        try {
            return Response.OK(null);
        } catch (Exception e) {
            log.error("Unexpected error when getting the RBK Details for current user", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get District List",
            notes = "Gets the District List Values",
            response = DistrictDetails.class
    )
    @GET
    @Path("/district")
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getDistrictLists() {
        try {
            return Response.OK(null);
        } catch (Exception e) {
            log.error("Unexpected error when getting the District List for current user", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get Mandal List",
            notes = "Gets the Mandal List w.r.t District-ID",
            response = MandalDetails.class
    )
    @GET
    @Path("/mandal/{districtId}")
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getMandalLists(@PathParam("districtId") String districtId) {
        try {
            return Response.OK(null);
        } catch (Exception e) {
            log.error("Unexpected error when getting the Get Mandal List for current user", e);
            return Response.INTERNAL_ERROR();
        }
    }
}