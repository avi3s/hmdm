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

package com.hmdm.plugins.deviceexport.rest.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Arrays;

/**
 * <p>A DTO carrying the details for request for exporting the devices.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceExportRequest implements Serializable {

    private static final long serialVersionUID = 222271229269359094L;
    /**
     * <p>A constant encoding the group filter type.</p>
     */
    private static final int GROUP_TYPE = 1;

    /**
     * <p>A constant encoding the configuration filter type.</p>
     */
    private static final int CONFIGURATION_TYPE = 2;

    /**
     * <p>A type of the filter to be used for filtering the device records for export.</p>
     *
     * @see #GROUP_TYPE
     * @see #CONFIGURATION_TYPE
     */
    private int filterType;

    /**
     * <p>A list of IDs of records of specified type to be used for filtering the device records for export.</p>
     */
    private int[] filterIds;

    /**
     * <p>An ID of a customer account to export devices for.</p>
     */
    private int customerId;

    /**
     * <p>An ID of a user exporting the devices.</p>
     */
    private int userId;

    /**
     * <p>A locale used for localizing the generated content.</p>
     */
    private String locale;

    /**
     * <p>A type of the export.</p>
     */
    private ExportType exportType;

    /**
     * <p>A list of exported columns.</p>
     */
    private String[] columns;

    /**
     * <p>Constructs new <code>DeviceExportRequest</code> instance. This implementation does nothing.</p>
     */
    public DeviceExportRequest() {
    }

    public int getFilterType() {
        return filterType;
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
    }

    public boolean isFilterByGroup () {
        return this.filterType == GROUP_TYPE;
    }

    public boolean getFilterByGroupRequired () {
        return isFilterByGroup() && this.filterIds.length > 0;
    }

    public boolean isFilterByConfiguration () {
        return this.filterType == CONFIGURATION_TYPE;
    }

    public boolean getFilterByConfigurationRequired () {
        return isFilterByConfiguration() && this.filterIds.length > 0;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int[] getFilterIds() {
        return filterIds;
    }

    public void setFilterIds(int[] filterIds) {
        this.filterIds = filterIds;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public ExportType getExportType() {
        return exportType;
    }

    public void setExportType(ExportType exportType) {
        this.exportType = exportType;
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "DeviceExportRequest{" +
                "filterType=" + filterType +
                ", locale=" + locale +
                ", exportType=" + exportType +
                ", filterIds=" + Arrays.toString(filterIds) +
                ", columns=" + Arrays.toString(columns) +
                '}';
    }
}
