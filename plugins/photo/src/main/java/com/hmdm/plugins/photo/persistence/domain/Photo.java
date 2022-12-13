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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmdm.persistence.domain.CustomerData;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * <p>A domain object representing a single photo.</p>
 *
 * @author isv
 */
@ApiModel(description = "A photo uploaded from device to MDM server")
@JsonIgnoreProperties(value = {"path", "thumbnailImagePath", "contentType"}, ignoreUnknown = true)
public class Photo implements CustomerData, Serializable {

    private static final long serialVersionUID = -3835563285891768353L;
    
    /**
     * <p>An ID of a photo.</p>
     */
    @ApiModelProperty("An ID of a photo")
    private Integer id;

    /**
     * <p>An URL providing the path to a file with photo image.</p>
     */
    @ApiModelProperty("An URL providing the path to a file with photo image")
    private String path;

    /**
     * <p>A latitude coordinate of the origination of the photo.</p>
     */
    @ApiModelProperty("A latitude coordinate of the origination of the photo")
    private Double lat;

    /**
     * <p>A longitude coordinate of the origination of the photo.</p>
     */
    @ApiModelProperty("A longitude coordinate of the origination of the photo")
    private Double lng;

    /**
     * <p>A timestamp of photo creation.</p>
     */
    @ApiModelProperty("A timestamp of photo creation")
    private Date createTime;

    /**
     * <p>An ID of a device related to this photo.</p>
     */
    @ApiModelProperty("An ID of a device related to this photo")
    private Integer deviceId;

    /**
     * <p>An ID of a customer account which the photo belongs to.</p>
     */
    @ApiModelProperty(hidden = true)
    private int customerId;

    /**
     * <p>A MIME type for the photo image file.</p>
     */
    @ApiModelProperty("A MIME type for the photo image file")
    private String contentType;

    /**
     * <p>A path to thumbnail image for this photo.</p>
     */
    @ApiModelProperty("A path to thumbnail image for this photo")
    private String thumbnailImagePath;

    /**
     * <p>A number (textual identifier) of related device. This is not a persisted field. But it gets populated based on
     * the details of related device.</p>
     */
    @ApiModelProperty("A number (textual identifier) of related device")
    private String deviceNumber;

    /**
     * <p>An optional address describing the photo location.</p>
     */
    @ApiModelProperty("An optional address describing the photo location")
    private String address;

    /**
     * <p>An optional address describing the location of the nearest pre-defined place.</p>
     */
    @ApiModelProperty("An optional address describing the location of the nearest pre-defined place")
    private String pointAddress;

    /**
     * <p>An optional address describing the location of the nearest pre-defined place.</p>
     */
    @ApiModelProperty("An optional ID of the nearest pre-defined place")
    private String pointId;

    /**
     * <p>Constructs new <code>Photo</code> instance. This implementation does nothing.</p>
     */
    public Photo() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    @Override
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getThumbnailImagePath() {
        return thumbnailImagePath;
    }

    public void setThumbnailImagePath(String thumbnailImagePath) {
        this.thumbnailImagePath = thumbnailImagePath;
    }

    public boolean isPreviewAvailable() {
        return this.thumbnailImagePath != null && !this.thumbnailImagePath.isEmpty();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * <p>Gets the suggested name for this photo when downloading it from server.</p>
     *
     * @return a suggested name for download copy of this photo.
     */
    public String getDownloadName() {
        String path = getPath();
        int pos = path.lastIndexOf(File.separatorChar);
        if (pos != -1 && pos != path.length() - 1) {
            return path.substring(pos + 1);
        } else {
            return getDefaultDownloadName();
        }
    }

    private String getDefaultDownloadName() {
        String s;
        boolean byAddress = false;
        if (this.address != null && !this.address.isEmpty()) {
            s = this.address;
            byAddress = true;
        } else {
            s = this.deviceNumber;
        }

        s = s.replaceAll("[^A-Za-z0-9а-яА-Я]", "_");

        while (s.contains("__")) {
            s = s.replace("__", "_");
        }

        if (byAddress) {
            s = s.substring(0, Math.min(40, s.length()));
        }

        return s + "_" + new SimpleDateFormat("yyMMdd_HHmm").format(this.createTime);
    }

    public String getPointAddress() {
        return pointAddress;
    }

    public void setPointAddress(String pointAddress) {
        this.pointAddress = pointAddress;
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return id == photo.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", createTime=" + createTime +
                ", deviceId=" + deviceId +
                ", deviceNumber='" + deviceNumber + '\'' +
                ", customerId=" + customerId +
                ", contentType=" + contentType +
                ", thumbnailImagePath=" + thumbnailImagePath +
                ", address=" + address +
                ", pointAddress=" + pointAddress +
                ", pointId" + pointId +
                '}';
    }
}
