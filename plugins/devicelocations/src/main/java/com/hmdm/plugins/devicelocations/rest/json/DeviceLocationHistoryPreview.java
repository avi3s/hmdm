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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>$</p>
 *
 * @author isv
 */
@ApiModel("")
public class DeviceLocationHistoryPreview {

    @ApiModelProperty("A total number of records matching the query")
    private final int totalRecordsCount;

    @ApiModelProperty("A number of matching records in a single chunk which may be retrieved from server")
    private final int pageSize;

    @ApiModelProperty("A total number of chunks of date for retrieval of records matching the query")
    private final int pageCount;

    /**
     * <p>Constructs new <code>DeviceLocationHistoryPreview</code> instance. This implementation does nothing.</p>
     */
    public DeviceLocationHistoryPreview(int totalRecordsCount, int pageSize) {
        this.totalRecordsCount = totalRecordsCount;
        this.pageSize = pageSize;
        this.pageCount = (int) Math.ceil(totalRecordsCount * 1D / pageSize);
    }

    public int getTotalRecordsCount() {
        return totalRecordsCount;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPageCount() {
        return pageCount;
    }
}
