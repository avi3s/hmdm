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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>A wrapper around the {@link Device} object providing the view suitable for the <code>Device List</code> view of
 * <code>Device Location</code>  plugin.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(value = {"device"}, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "A device registered to MDM server and running the MDM mobile application")
public class DeviceView {

    /**
     * <p>A device data.</p>
     */
    private final Device device;

    /**
     * <p>Constructs new <code>DeviceView</code> instance. This implementation does nothing.</p>
     */
    public DeviceView(Device device) {
        this.device = device;
    }

    @ApiModelProperty("An ID of device")
    public Integer getId() {
        return device.getId();
    }

    @ApiModelProperty("An unique textual identifier of device")
    public String getNumber() {
        return device.getNumber();
    }

    @ApiModelProperty("An IMEI of device as set by the administrator")
    public String getImei() {
        return device.getImei();
    }

    @ApiModelProperty("A description of device as set by the administrator")
    public String getDescription() {
        return device.getDescription();
    }
}
