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

package com.hmdm.plugins.deviceimport.service;

import com.hmdm.plugins.deviceimport.rest.json.DeviceImportRequest;
import com.hmdm.plugins.deviceimport.rest.json.DeviceImportStatusItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * <p>An interface for the parsers of the list of devices to be imported.</p>
 */
public interface DeviceListParser {

//    /**
//     * <p>Parses the uploaded list of devices and analyzes the list of extracted devices to be imported into DB.</p>
//     *
//     * @param request the parameters of device import process.
//     * @return the results of parsing and analyzing the details for devices to be imported.
//     */
//    List<DeviceImportStatusItem> prepareImport(DeviceImportRequest request) throws IOException;

    /**
     * <p>Parses the uploaded list of devices and analyzes the list of extracted devices to be imported into DB.</p>
     *
     * @param request the parameters of device import process.
     * @param devicesListContent the content of the list with devices data.
     * @return the results of parsing and analyzing the details for devices to be imported.
     */
    List<DeviceImportStatusItem> parseDeviceList(DeviceImportRequest request, InputStream devicesListContent) throws IOException;

}
