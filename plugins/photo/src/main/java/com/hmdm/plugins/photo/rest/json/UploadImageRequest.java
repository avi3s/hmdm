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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>A DTO representing a request for uploading new photo from device to server.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadImageRequest {

    // A latitude coordinate
    private Double lat;

    // A longitude coordinate
    private Double lng;

    // A time of creation of photo in milliseconds from epoch time
    private Long createTime;

    // An optional address for the photo location.
    private String address;

    // An optional address for the nearest place location.
    private String pointAddress;

    // An optional ID for the nearest place.
    private String pointId;

    /**
     * <p>Constructs new <code>UploadImageRequest</code> instance. This implementation does nothing.</p>
     */
    public UploadImageRequest() {
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

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
    public String toString() {
        return "UploadImageRequest{" +
                "lat=" + lat +
                ", lng=" + lng +
                ", createTime=" + createTime +
                ", address=" + address +
                ", pointAddress=" + pointAddress +
                ", pointId=" + pointId +
                '}';
    }
}
