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
import javax.inject.Named;
import javax.inject.Singleton;

import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.plugins.photo.persistence.DuplicatePhotoException;
import com.hmdm.plugins.photo.persistence.PhotoPluginSettingsDAO;
import com.hmdm.plugins.photo.persistence.PlaceDAO;
import com.hmdm.plugins.photo.persistence.domain.PhotoPluginSettings;
import com.hmdm.util.BackgroundTaskRunnerService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.ResponseHeader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.plugins.photo.persistence.PhotoDAO;
import com.hmdm.plugins.photo.persistence.domain.Photo;
import com.hmdm.plugins.photo.rest.json.PhotoFilter;
import com.hmdm.plugins.photo.rest.json.UploadImageRequest;
import com.hmdm.plugins.photo.task.ThumbnailImageGenerationTask;
import com.hmdm.rest.json.DeviceLookupItem;
import com.hmdm.rest.json.PaginatedData;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import com.hmdm.util.CryptoUtil;
import com.hmdm.util.StringUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.hmdm.plugins.photo.PluginConfigurationImpl.PLUGIN_ID;
import static com.hmdm.util.FileUtil.writeToFile;

/**
 * <p>A resource to be used for accessing the data for <code>Photo</code> records.</p>
 *
 * @author isv
 */
@Api(tags = {"Plugin - Photo"})
@Singleton
@Path("/plugins/photo/photo")
public class PhotoResource {

    // A logging service
    private static final Logger logger  = LoggerFactory.getLogger(PhotoResource.class);

    // AN executor for the thumbnail generation tasks
    private BackgroundTaskRunnerService executor;

    // A secret used for verifying the hashes for the requests from devices
    private String hashSecret;

    // A base directory for file storage
    private String filesDirectory;

    // An unsecure DAO for getting the data from DB
    private UnsecureDAO unsecureDAO;

    // A DAO for managing the photo objects in DB
    private PhotoDAO photoDAO;

    // A DAO for managing the plugin settings
    private PhotoPluginSettingsDAO settingsDAO;

    // A DAO for managing the device objects in DB
    private DeviceDAO deviceDAO;

    // A DAO for managing the place objects in DB
    private PlaceDAO placeDAO;

    private PluginStatusCache pluginStatusCache;

    // A mapping from device ID to details for the latest photo sent by device
    private static final Map<String, String> lastDevicePhotos = new ConcurrentHashMap<>(200);

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public PhotoResource() {
    }

    /**
     * <p>Constructs new <code>PhotoResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PhotoResource(@Named("hash.secret") String hashSecret,
                         @Named("plugins.files.directory") String filesDirectory,
                         UnsecureDAO unsecureDAO,
                         PhotoDAO photoDAO,
                         PhotoPluginSettingsDAO settingsDAO,
                         DeviceDAO deviceDAO,
                         PlaceDAO placeDAO,
                         BackgroundTaskRunnerService executor,
                         PluginStatusCache pluginStatusCache) {
        this.hashSecret = hashSecret;
        this.filesDirectory = filesDirectory;
        this.unsecureDAO = unsecureDAO;
        this.photoDAO = photoDAO;
        this.settingsDAO = settingsDAO;
        this.deviceDAO = deviceDAO;
        this.placeDAO = placeDAO;
        this.executor = executor;
        this.pluginStatusCache = pluginStatusCache;
    }

    /**
     * <p>Gets the list of photos matching the specified filter.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a response with list of photos matching the specified filter.
     */
    @ApiOperation(
            value = "Search photos",
            notes = "Gets the list of photos matching the specified filter",
            response = PaginatedData.class,
            authorizations = {@Authorization("Bearer Token")}
    )
    @POST
    @Path("/private/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPhotos(PhotoFilter filter) {

        List<Photo> photos = this.photoDAO.findAll(filter);
        long count = this.photoDAO.countAll(filter);

        return Response.OK(new PaginatedData<>(photos, count));
    }

    /**
     * <p>Gets the list of devices matching the specified filter.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a response with list of devices matching the specified filter.
     */
    @ApiOperation(value = "", hidden = true)
    @POST
    @Path("/private/device/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevices(PhotoFilter filter) {
        List<DeviceLookupItem> devices = this.deviceDAO.findDevices(filter.getDeviceFilter(), filter.getPageSize());
        return Response.OK(devices);
    }

    /**
     * <p>Gets the list of devices matching the specified filter.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a response with list of devices matching the specified filter.
     */
    @ApiOperation(value = "", hidden = true)
    @POST
    @Path("/private/place/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlaces(PhotoFilter filter) {
        try {
            List<String> places = this.placeDAO.findPlaces(filter.getPointFilter(), filter.getPageSize());
            return Response.OK(places);
        } catch (Exception e) {
            logger.error("Unexpected error when looking up for places", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Deletes the photo referenced by the ID.</p>
     *
     * @param id an ID of photo to be deleted.
     * @return a response with status of deletion.
     */
    @ApiOperation(
            value = "Delete photo",
            notes = "Deletes the photo referenced by the ID",
            authorizations = {@Authorization("Bearer Token")}
    )
    @DELETE
    @Path("/private/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePhoto(@PathParam("id") @ApiParam("Photo ID") int id) {
        if (SecurityContext.get().hasPermission("plugin_photo_remove_photo")) {
            this.photoDAO.deletePhoto(id);
            logger.info("User {} deleted photo #{}", SecurityContext.get().getCurrentUser().get().getLogin(), id);
            return Response.OK();
        } else {
            return Response.PERMISSION_DENIED();
        }
    }

    /**
     * <p>Gets the content of the image for the photo referenced by the ID.</p>
     *
     * @param id an ID of photo to get image content for.
     */
    @ApiOperation(
            value = "Get photo",
            notes = "Gets the content of the image for the photo referenced by the ID",
            responseHeaders = {@ResponseHeader(name = "Content-Type"), @ResponseHeader(name = "Content-Length")},
            authorizations = {@Authorization("Bearer Token")}
    )
    @GET
    @Path("/private/{id}/{save-as-name}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public javax.ws.rs.core.Response getImage(@PathParam("id") @ApiParam("Photo ID") int id,
                                              @PathParam("save-as-name")
                                              @ApiParam("A name to be used when saving the photo image to local disk")
                                                      String saveAsName) throws IOException {
        return getFileContent(id, Photo::getPath, Photo::getContentType);
    }

    /**
     * <p>Gets the content of the thumbnail image for the photo referenced by the ID.</p>
     *
     * @param id an ID of photo to get thumbnail image content for.
     */
    @ApiOperation(
            value = "Get photo preview",
            notes = "Gets the content of the thumbnail image for the photo referenced by the ID",
            responseHeaders = {@ResponseHeader(name = "Content-Type"), @ResponseHeader(name = "Content-Length")},
            authorizations = {@Authorization("Bearer Token")}
    )
    @GET
    @Path("/private/{id}/preview")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public javax.ws.rs.core.Response getThumbnailImage(@PathParam("id") @ApiParam("Photo ID") int id) throws IOException {
        return getFileContent(id, Photo::getThumbnailImagePath, photo -> "image/png");
    }

    /**
     * <p>Uploads the photo image from device to server and generates the thumbnail preview image for it.</p>
     *
     * @param uploadedInputStream a stream with photo image content.
     * @param fileDetail a form data related to photo image file.
     * @param body a body of request.
     * @param details a details for the photo image.
     * @param deviceId a device number identifying the device.
     * @param hash a hash value to be used for verifying the request.
     * @return a response to be returned to client.
     * @throws Exception if an unexpected error occurs.
     */
    @ApiOperation(
            value = "UploadPhoto",
            notes = "Uploads the photo image from device to server"
    )
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/upload")
    public Response uploadFiles(@FormDataParam("photo") InputStream uploadedInputStream,
                                @ApiParam("A photo image file to upload") @FormDataParam("photo") FormDataContentDisposition fileDetail,
                                @FormDataParam("photo") final FormDataBodyPart body,
                                @ApiParam("A detail for the photo image") @FormDataParam("details") String details,
                                @ApiParam("An unique textual identifier of device") @FormDataParam("deviceId") String deviceId,
                                @ApiParam("A hash value to be used for verifying the request") @FormDataParam("hash") String hash) throws Exception {

        // For some reason, the browser sends the file name in ISO_8859_1, so we use a workaround to convert
        // it to UTF_8 and enable non-ASCII characters
        // https://stackoverflow.com/questions/50582435/jersey-filename-encoded
        String fileName = fileDetail == null ? "null" : new String(fileDetail.getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        logger.info("Received Upload Photo request. DeviceId: {}, details: {}, hash: {}, file: {}",
                deviceId, details, hash, fileName);

        deviceId = StringUtil.stripOffTrailingCharacter(deviceId, "\"");
        hash = StringUtil.stripOffTrailingCharacter(hash, "\"");

        ObjectMapper objectMapper = new ObjectMapper();
        UploadImageRequest request = objectMapper.readValue(details, UploadImageRequest.class);

        List<String> errors = new ArrayList<>();
        if (request.getCreateTime() == null) {
            errors.add("createTime");
        }
        if (!(fileDetail != null && !fileName.isEmpty() && uploadedInputStream != null)) {
            errors.add("photo");
        }
        if (deviceId == null || deviceId.isEmpty()) {
            errors.add("deviceId");
        }
        if (hash == null || hash.isEmpty()) {
            errors.add("hash");
        }

        if (!errors.isEmpty()) {
            return Response.ERROR("error.params.missing", errors);
        }

        String expectedHash = CryptoUtil.getMD5String(deviceId + this.hashSecret);
        if (!expectedHash.equalsIgnoreCase(hash)) {
            logger.error("Hash invalid for upload photo request from device {}. Expected: {} but got {}",
                    deviceId, expectedHash, hash.toUpperCase());
//            System.out.println("Hash invalid for device " + deviceId + ": " + expectedHash + " vs " + hash);
//            return Response.ERROR("Invalid hash value");
        }

        // Find device and get the customer
        Device dbDevice = this.unsecureDAO.getDeviceByNumber(deviceId);
        if (dbDevice == null) {
            return Response.DEVICE_NOT_FOUND_ERROR();
        }

        if (this.pluginStatusCache.isPluginDisabled(PLUGIN_ID)) {
            logger.error("Rejecting request from device {} due to disabled plugin", deviceId);
            return Response.PLUGIN_DISABLED();
        }
        PhotoPluginSettings settings = settingsDAO.getPluginSettings(dbDevice.getCustomerId());

        // Check the photo against the latest sent by device and reject adding it if it was sent already

        // Variant #1
        String photoSignature = request.getCreateTime() + "#";
        final String previous = lastDevicePhotos.get(deviceId);
        if (previous != null && previous.equals(photoSignature)) {
            logger.error("Duplicate photo image from device {} detected. Skipping processing. Photo details: {}",
                    deviceId, details);
            return Response.OK();
        } else {
            lastDevicePhotos.put(deviceId, photoSignature);
        }

        // Variant #2
        long count = this.photoDAO.countPhotos(dbDevice.getId(), new Date(request.getCreateTime()));

        if (count > 0) {
            logger.error("Duplicate photo image from device {} detected. Skipping processing. Photo details: {}. File: {}",
                    deviceId, details, fileName);
            return Response.OK();
        }

        try {
            DecimalFormat numberFormat = new DecimalFormat("##00");
            final LocalDateTime date = LocalDateTime.ofEpochSecond(request.getCreateTime() / 1000, 0, ZoneOffset.UTC);
            String year = numberFormat.format(date.getYear());
            String month = numberFormat.format(date.getMonthValue());
            String day = numberFormat.format(date.getDayOfMonth());
            String hour = numberFormat.format(date.getHour());
            String minute = numberFormat.format(date.getMinute());
            String second = numberFormat.format(date.getSecond());

            // Generate the name for file and Determine the directories to put the uploaded file to
            String originalFileName = fileName;
            String originalNameNoExt = originalFileName;
            int pos = originalFileName.lastIndexOf('.');
            String ext = null;
            if (pos != -1 && pos != originalFileName.length() - 1) {
                ext = originalFileName.substring(pos + 1);
                originalNameNoExt = originalFileName.substring(0, pos);
            }

            String localFileNameTemplate = settings != null && settings.getNameTemplate() != null ? settings.getNameTemplate() :
                    PhotoPluginSettings.DEFAULT_NAME_TEMPLATE;
            String address = request.getAddress() != null ? request.getAddress() : "";
            String localFileName = localFileNameTemplate
                    .replaceAll("DEVICE", dbDevice.getNumber())
                    .replaceAll("YEAR", year)
                    .replaceAll("MONTH", month)
                    .replaceAll("DAY", day)
                    .replaceAll("HOUR", hour)
                    .replaceAll("MIN", minute)
                    .replaceAll("SEC", second)
                    .replaceAll("EXT", ext != null ? ext : "null")
                    .replaceAll("NAME", originalNameNoExt)
                    .replaceAll("ADDRESS", address.replaceAll("[^A-Za-z0-9а-яА-Я]", "_"));
            String localFileNameMain = localFileName;
            if (ext != null) {
                localFileName += "." + ext;
            }

            String pathTemplate = settings != null && settings.getDirectory() != null ? settings.getDirectory() :
                    PhotoPluginSettings.DEFAULT_PATH_TEMPLATE.replace('/', File.separatorChar);
            String pathRelative = pathTemplate
                    .replaceAll("DEVICE", dbDevice.getNumber())
                    .replaceAll("YEAR", year)
                    .replaceAll("MONTH", month)
                    .replaceAll("DAY", day)
                    .replaceAll("EXT", ext != null ? ext : "null");
            String pathBase = "plugin" + File.separatorChar + "photo";

            java.nio.file.Path uploadFilePathDir = Paths.get(this.filesDirectory, pathBase, pathRelative);
            uploadFilePathDir = Files.createDirectories(uploadFilePathDir);

            File uploadFile = new File(uploadFilePathDir.toFile(), localFileName);

            // Save file to disk
            if (Files.exists(uploadFilePathDir) && Files.isDirectory(uploadFilePathDir)) {
                writeToFile(uploadedInputStream, uploadFile.getAbsolutePath());

                Photo photo = new Photo();
                photo.setCreateTime(new Date(date.toEpochSecond(ZoneOffset.UTC) * 1000 ));
                photo.setLat(request.getLat());
                photo.setLng(request.getLng());
                photo.setAddress(request.getAddress());
                photo.setCustomerId(dbDevice.getCustomerId());
                photo.setDeviceId(dbDevice.getId());
                photo.setPath(Paths.get(pathBase, pathRelative, localFileName).toString());
                photo.setContentType(body.getMediaType().toString());
                photo.setPointAddress(request.getPointAddress());
                photo.setPointId(request.getPointId());

                this.photoDAO.insertPhoto(photo);

                // Schedule a task to generate the thumbnail image for the uploaded file
                if (isImageContentType(photo.getContentType())) {
                    this.executor.submitTask(new ThumbnailImageGenerationTask(photo, this.filesDirectory, this.photoDAO, pathRelative, localFileNameMain));
                }

                return Response.OK();
            } else {
                logger.error("Error when processing the uploaded photo. The target directory is not available: {}", uploadFilePathDir);
                return Response.ERROR("error.file.save");
            }
        } catch (DuplicatePhotoException e) {
            logger.error("Duplicate photo image for device {} detected. Skipping processing. Photo details: {}. File: {}",
                    deviceId, details, fileName);
            return Response.OK();
        } catch (Exception e) {
            logger.error("Unexpected error when handling photo upload request", e);
            return Response.ERROR("error.file.save");
        }
    }

    private boolean isImageContentType(String contentType) {
        return contentType.equalsIgnoreCase("image/jpeg") ||
               contentType.equalsIgnoreCase("image/png");
    }

    /**
     * <p>Gets the content of the requested image for specified photo and sends it to client.</p>
     *
     * @param id an ID of a photo to get image content for.
     * @param pathToImage a function used to evaluate the path to image for requested photo.
     * @param imageContentType a function used to evaluate the content type to image for requested photo.
     * @return a response to be sent to client.
     * @throws IOException if an I/O error occurs while calculating the size of the image.
     */
    private javax.ws.rs.core.Response getFileContent(int id, Function<Photo, String> pathToImage,
                                                     Function<Photo, String> imageContentType) throws IOException {
        Optional<Photo> photoOpt = this.photoDAO.findById(id);
        if (photoOpt.isPresent()) {
            Photo photo = photoOpt.get();
            String imagePath = pathToImage.apply(photo);

            if (imagePath != null) {
                java.nio.file.Path path = Paths.get(this.filesDirectory, imagePath);
                File file = path.toFile();
                if (file.exists()) {
                    javax.ws.rs.core.Response.ResponseBuilder response = javax.ws.rs.core.Response.ok(file);
                    response.header("Content-Type", imageContentType.apply(photo));
                    response.header("Content-Length", Files.size(path));
                    return response.build();
                }
            }
        }

        return javax.ws.rs.core.Response.status(404).build();
    }
}
