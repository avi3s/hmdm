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

package com.hmdm.plugins.devicelocations.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>A domain object representing a single record on device location at some point in time.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceLocation implements Serializable {

    private static final long serialVersionUID = 2634901198463048889L;

    @ApiModelProperty("An ID of device location record")
    private int id;

    @ApiModelProperty("An ID of device")
    private int deviceId;

    @ApiModelProperty("A latitude coordinate")
    private double lat;

    @ApiModelProperty("A longitude coordinate")
    private double lon;

    @ApiModelProperty("A timestamp of location recording by device (in milliseconds since epoch time)")
    private long ts;

    /**
     * <p>Constructs new <code>DeviceLocation</code> instance. This implementation does nothing.</p>
     */
    public DeviceLocation() {
    }

    public DeviceLocation(com.hmdm.rest.json.DeviceLocation location, int deviceId) {
        this.deviceId = deviceId;
        this.lat = location.getLat();
        this.lon = location.getLon();
        this.ts = location.getTs();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "DeviceLocation{" +
                "id=" + id +
                ", deviceId=" + deviceId +
                ", lat=" + lat +
                ", lon=" + lon +
                ", ts=" + ts +
                '}';
    }
}
