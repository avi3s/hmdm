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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>A DTO carrying the details for a single place </p>
 *
 * @author isv
 */
@ApiModel(description = "A place found around some cooridnates")
public class PlaceSearchResultItem implements Serializable {

    private static final long serialVersionUID = -5019336359060698705L;

    @ApiModelProperty("A place ID")
    private String pointId;
    @ApiModelProperty("A place address")
    private String pointAddress;
    @ApiModelProperty("A latitude coordinate for place location")
    private double lat;
    @ApiModelProperty("A longitude coordinate for place location")
    private double lng;
    @ApiModelProperty("A distance to target location (in meters)")
    private double distance;

    /**
     * <p>Constructs new <code>PlaceSearchResultItem</code> instance. This implementation does nothing.</p>
     */
    public PlaceSearchResultItem() {
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public String getPointAddress() {
        return pointAddress;
    }

    public void setPointAddress(String pointAddress) {
        this.pointAddress = pointAddress;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "PlaceSearchResultItem{" +
                "pointId='" + pointId + '\'' +
                ", pointAddress='" + pointAddress + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", distance=" + distance +
                '}';
    }
}
