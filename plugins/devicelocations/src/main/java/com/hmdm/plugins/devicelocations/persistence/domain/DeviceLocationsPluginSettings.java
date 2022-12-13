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
import com.hmdm.persistence.domain.CustomerData;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>A domain object representing a single collection of <code>Device Locations</code> plugin settings per customer
 * account.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true, value = {"customerId"})
public class DeviceLocationsPluginSettings implements CustomerData, Serializable {

    private static final long serialVersionUID = -47583357143475933L;

    /**
     * <p>An ID of a setting record.</p>
     */
    @ApiModelProperty("An ID of a setting record.")
    private Integer id;

    /**
     * <p>An ID of a customer account which the record belongs to.</p>
     */
    @ApiModelProperty(hidden = true)
    private int customerId;

    /**
     * <p>A period for preserving the data records in persistent data store (in days).</p>
     */
    @ApiModelProperty(value = "A period for preserving the data records in persistent data store (in days)", required = true)
    private int dataPreservePeriod = 30;

    /**
     * <<p>An URL for map tile server</p>
     */
    @ApiModelProperty(value = "An URL for map tile server", required = true)
    private String tileServerUrl = "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";

    /**
     * <p>Minimal time in seconds between updates (0 - no additional updates).</p>
     */
    @ApiModelProperty(value = "Minimal time in seconds between updates (0 - no additional updates)", required = true)
    private int updateTime = 30;

    /**
     * <p>Constructs new <code>DeviceLocationsPluginSettings</code> instance. This implementation does nothing.</p>
     */
    public DeviceLocationsPluginSettings() {
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int getCustomerId() {
        return customerId;
    }

    @Override
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getDataPreservePeriod() {
        return dataPreservePeriod;
    }

    public void setDataPreservePeriod(int dataPreservePeriod) {
        this.dataPreservePeriod = dataPreservePeriod;
    }

    public String getTileServerUrl() {
        return tileServerUrl;
    }

    public void setTileServerUrl(String tileServerUrl) {
        this.tileServerUrl = tileServerUrl;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "DeviceLocationsPluginSettings{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", dataPreservePeriod=" + dataPreservePeriod +
                ", tileServerUrl='" + tileServerUrl + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }
}
