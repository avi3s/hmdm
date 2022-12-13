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

package com.hmdm.plugins.photo.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.hmdm.persistence.domain.CustomerData;

import java.io.Serializable;

/**
 * <p>A domain object representing a single collection of plugin settings per customer account.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "A collection of 'Photo' plugin settings")
public class PhotoPluginSettings implements CustomerData, Serializable {

    private static final long serialVersionUID = 6862393507101176771L;

    public static final String DEFAULT_PATH_TEMPLATE = "YEAR/MONTH/DAY";
    public static final String DEFAULT_NAME_TEMPLATE = "DEVICE_YEARMONTHDAY_HOURMINSEC";

    @ApiModelProperty("An ID of the record")
    private Integer id;

    @ApiModelProperty("A flag indicating if the location of the device needs to be recorded")
    private boolean trackLocation = true;

    @ApiModelProperty("A message to display to user in case the location tracking is switched off")
    private String trackingOffWarning;

    // An ID of a customer account which these settings correspond to
    @ApiModelProperty(hidden = true)
    private int customerId;

    @ApiModelProperty("A flag indicating if the photo needs to be sent to server")
    private boolean sendPhoto;

    @ApiModelProperty("The paths to images (separated with ;)")
    private String imagePaths;

    @ApiModelProperty("Paths that do not require transmission (separated ;)")
    private String nonTransmittedPaths;

    @ApiModelProperty("A flag indicating if standard image paths are to be included")
    private boolean includeStandardImagePaths;

    @ApiModelProperty("A delay before deleting the images (in seconds)")
    private Integer imageDeletionDelay;

    @ApiModelProperty("A flag indicating if a text needs to be added to photo")
    private boolean addText;

    @ApiModelProperty("A background color")
    private String backgroundColor;

    @ApiModelProperty("A text color")
    private String textColor;

    @ApiModelProperty("A transparency for the background")
    private Integer transparency;

    @ApiModelProperty("A text to be added to photo")
    private String textContent;

    @ApiModelProperty("A flag indicating if the photo needs to be linked to pre-defined place")
    private boolean linkPhotoToPlace;

    @ApiModelProperty("A radius for searching the nearest place for photo (in meters)")
    private int searchPlaceRadius;

    @ApiModelProperty(hidden = true)
    private boolean featurePlacesEnabled;

    @ApiModelProperty("Types of non-image files to transfer, comma-separated")
    private String fileTypes;

    @ApiModelProperty("Directory structure on the server")
    private String directory;

    @ApiModelProperty("Remove files older than this amount of days (0 - do not remove)")
    private int purgeDays;

    @ApiModelProperty("Template for file names on the server")
    private String nameTemplate;

    /**
     * <p>Constructs new <code>PhotoPluginSettings</code> instance. This implementation does nothing.</p>
     */
    public PhotoPluginSettings() {
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isTrackLocation() {
        return trackLocation;
    }

    public void setTrackLocation(boolean trackLocation) {
        this.trackLocation = trackLocation;
    }

    public String getTrackingOffWarning() {
        return trackingOffWarning;
    }

    public void setTrackingOffWarning(String trackingOffWarning) {
        this.trackingOffWarning = trackingOffWarning;
    }

    public boolean isSendPhoto() {
        return sendPhoto;
    }

    public void setSendPhoto(boolean sendPhoto) {
        this.sendPhoto = sendPhoto;
    }

    @Override
    public int getCustomerId() {
        return customerId;
    }

    @Override
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(String imagePaths) {
        this.imagePaths = imagePaths;
    }

    public Integer getImageDeletionDelay() {
        return imageDeletionDelay;
    }

    public void setImageDeletionDelay(Integer imageDeletionDelay) {
        this.imageDeletionDelay = imageDeletionDelay;
    }

    public boolean isAddText() {
        return addText;
    }

    public void setAddText(boolean addText) {
        this.addText = addText;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public Integer getTransparency() {
        return transparency;
    }

    public void setTransparency(Integer transparency) {
        this.transparency = transparency;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public boolean isLinkPhotoToPlace() {
        return linkPhotoToPlace;
    }

    public void setLinkPhotoToPlace(boolean linkPhotoToPlace) {
        this.linkPhotoToPlace = linkPhotoToPlace;
    }

    public int getSearchPlaceRadius() {
        return searchPlaceRadius;
    }

    public void setSearchPlaceRadius(int searchPlaceRadius) {
        this.searchPlaceRadius = searchPlaceRadius;
    }

    public boolean isFeaturePlacesEnabled() {
        return featurePlacesEnabled;
    }

    public void setFeaturePlacesEnabled(boolean featurePlacesEnabled) {
        this.featurePlacesEnabled = featurePlacesEnabled;
    }

    public String getNonTransmittedPaths() {
        return nonTransmittedPaths;
    }

    public void setNonTransmittedPaths(String nonTransmittedPaths) {
        this.nonTransmittedPaths = nonTransmittedPaths;
    }

    public boolean isIncludeStandardImagePaths() {
        return includeStandardImagePaths;
    }

    public void setIncludeStandardImagePaths(boolean includeStandardImagePaths) {
        this.includeStandardImagePaths = includeStandardImagePaths;
    }

    public String getFileTypes() {
        return fileTypes;
    }

    public void setFileTypes(String fileTypes) {
        this.fileTypes = fileTypes;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public int getPurgeDays() {
        return purgeDays;
    }

    public void setPurgeDays(int purgeDays) {
        this.purgeDays = purgeDays;
    }

    public String getNameTemplate() {
        return nameTemplate;
    }

    public void setNameTemplate(String nameTemplate) {
        this.nameTemplate = nameTemplate;
    }

    @Override
    public String toString() {
        return "PhotoPluginSettings{" +
                "id=" + id +
                ", trackLocation=" + trackLocation +
                ", trackingOffWarning='" + trackingOffWarning + '\'' +
                ", customerId=" + customerId +
                ", sendPhoto=" + sendPhoto +
                ", imagePaths='" + imagePaths + '\'' +
                ", nonTransmittedPaths='" + nonTransmittedPaths + '\'' +
                ", includeStandardImagePaths='" + includeStandardImagePaths + '\'' +
                ", imageDeletionDelay=" + imageDeletionDelay +
                ", addText=" + addText +
                ", backgroundColor='" + backgroundColor + '\'' +
                ", textColor='" + textColor + '\'' +
                ", transparency=" + transparency +
                ", textContent='" + textContent + '\'' +
                ", linkPhotoToPlace='" + linkPhotoToPlace + '\'' +
                ", searchPlaceRadius='" + searchPlaceRadius + '\'' +
                ", fileTypes='" + fileTypes + '\'' +
                ", directory='" + directory + '\'' +
                ", purgeDays=" + purgeDays +
                ", nameTemplate='" + nameTemplate + '\'' +
                '}';
    }
}
