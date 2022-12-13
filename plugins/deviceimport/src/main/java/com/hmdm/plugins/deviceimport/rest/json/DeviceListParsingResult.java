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

package com.hmdm.plugins.deviceimport.rest.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

/**
 * <p>A DTO carrying the details on parsing the list with device details to be imported.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceListParsingResult implements Serializable {

    private static final long serialVersionUID = -8434131305992008759L;
    
    /**
     * <p>An ID identifying this result among others.</p>
     */
    private final String uuid;

    /**
     * <p>A list of devices parsed from the Excel file.</p>
     */
    private final List<DeviceImportStatusItem> devices;

    /**
     * <p>Constructs new <code>DeviceListParsingResult</code> instance. This implementation does nothing.</p>
     */
    public DeviceListParsingResult(String uuid, List<DeviceImportStatusItem> devices) {
        this.uuid = uuid;
        this.devices = devices;
    }

    public String getUuid() {
        return uuid;
    }

    public List<DeviceImportStatusItem> getDevices() {
        return devices;
    }

    @Override
    public String toString() {
        return "DeviceListParsingResult{" +
                "uuid='" + uuid + '\'' +
                ", devices=" + devices +
                '}';
    }
}
