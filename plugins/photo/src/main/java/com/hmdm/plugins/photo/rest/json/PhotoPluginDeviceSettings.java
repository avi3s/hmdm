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

package com.hmdm.plugins.photo.rest.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmdm.plugins.photo.persistence.domain.PhotoPluginSettings;

/**
 * <p>A DTO providing the <code>Photo Plugin</code> settings to be returned in response to requests from devices.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(value = {"searchPlaceRadius"}, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "A photo plugin settings to be used on mobile device")
public class PhotoPluginDeviceSettings {

    @JsonIgnore
    private final PhotoPluginSettings wrapped;

    @JsonIgnore
    private final boolean featurePlacesEnabled;

    /**
     * <p>Constructs new <code>PhotoPluginDeviceSettings</code> instance. This implementation does nothing.</p>
     */
    public PhotoPluginDeviceSettings(PhotoPluginSettings settings, boolean featurePlacesEnabled) {
        this.wrapped = settings;
        this.featurePlacesEnabled = featurePlacesEnabled;
    }

    @ApiModelProperty("A flag indicating if the location of the device needs to be recorded")
    public boolean isTrackLocation() {
        return this.wrapped.isTrackLocation();
    }

    @ApiModelProperty("A message to display to user in case the location tracking is switched off")
    public String getTrackingOffWarning() {
        return this.wrapped.getTrackingOffWarning();
    }

    @ApiModelProperty("A flag indicating if the photo needs to be sent to server")
    public boolean isSendPhoto() {
        return this.wrapped.isSendPhoto();
    }

    @ApiModelProperty("The paths to images (separated with ;)")
    public String getImagePaths() {
        return this.wrapped.getImagePaths();
    }

    @ApiModelProperty("A delay before deleting the images (in seconds)")
    public Integer getImageDeletionDelay() {
        return this.wrapped.getImageDeletionDelay();
    }

    @ApiModelProperty("A flag indicating if a text needs to be added to photo")
    public boolean isAddText() {
        return this.wrapped.isAddText();
    }

    @ApiModelProperty("A background color")
    public String getBackgroundColor() {
        return this.wrapped.getBackgroundColor();
    }

    @ApiModelProperty("A text color")
    public String getTextColor() {
        return this.wrapped.getTextColor();
    }

    @ApiModelProperty("A transparency for the background")
    public Integer getTransparency() {
        return this.wrapped.getTransparency();
    }

    @ApiModelProperty("A text to be added to photo")
    public String getTextContent() {
        return this.wrapped.getTextContent();
    }

    @ApiModelProperty("A flag indicating if the photo needs to be linked to pre-defined place")
    public Boolean isLinkPhotoToPlace() {
        if (this.featurePlacesEnabled) {
            return this.wrapped.isLinkPhotoToPlace();
        } else {
            return null;
        }
    }

    @ApiModelProperty("A radius for searching the nearest place for photo (in meters)")
    public Integer getSearchPlaceRadius() {
        if (this.featurePlacesEnabled) {
            return this.wrapped.getSearchPlaceRadius();
        } else {
            return null;
        }
    }

    @ApiModelProperty("Paths that do not require transmission (separated ;)")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getNonTransmittedPaths() {
        return wrapped.getNonTransmittedPaths();
    }

    @ApiModelProperty("A flag indicating if standard image paths are to be included")
    public boolean isIncludeStandardImagePaths() {
        return wrapped.isIncludeStandardImagePaths();
    }

    @ApiModelProperty("Additional file types to transmit (separated by ;)")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getFileTypes() {
        return wrapped.getFileTypes();
    }
}
