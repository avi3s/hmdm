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

package com.hmdm.plugins.devicelocations.rest.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.persistence.domain.Device;
import com.hmdm.plugins.devicelocations.persistence.domain.DeviceLocation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>A wrapper around the {@link DeviceLocation} object providing the view suitable for the <code>Device List</code>
 * view of <code>Device Location</code>  plugin.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(value = {"location"}, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "The details on location of device")
public class DeviceLocationView {

    /**
     * <p>Device location data.</p>
     */
    private final DeviceLocation location;

    /**
     * <p>Constructs new <code>DeviceLocationView</code> instance. This implementation does nothing.</p>
     */
    public DeviceLocationView(DeviceLocation location) {
        this.location = location;
    }

    @ApiModelProperty("A latitude coordinate")
    public double getLat() {
        return this.location.getLat();
    }

    @ApiModelProperty("A longitude coordinate")
    public double getLon() {
        return this.location.getLon();
    }

    @ApiModelProperty("A timestamp of location recording by device (in milliseconds since epoch time)")
    public long getTs() {
        return this.location.getTs();
    }
}
