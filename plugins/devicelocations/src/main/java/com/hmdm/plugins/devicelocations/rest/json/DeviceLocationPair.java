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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * <p>A DTO carrying the details on device and it's location.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "A device and it's location")
public class DeviceLocationPair {

    @ApiModelProperty("The details for device")
    private final DeviceView device;

    @ApiModelProperty("The details for device location")
    private final DeviceLocationView location;

    /**
     * <p>Constructs new <code>DeviceLocationPair</code> instance. This implementation does nothing.</p>
     */
    public DeviceLocationPair(@NotNull DeviceView device,
                              @NotNull DeviceLocationView location) {
        this.device = device;
        this.location = location;
    }

    public DeviceView getDevice() {
        return device;
    }

    public DeviceLocationView getLocation() {
        return location;
    }
}
