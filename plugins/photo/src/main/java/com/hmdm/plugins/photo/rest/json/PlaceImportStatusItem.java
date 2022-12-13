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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * <p>A DTO carrying the details on a single place with details parsed from Excel file.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlaceImportStatusItem implements Serializable {

    private static final long serialVersionUID = 5365482419505431864L;
    /**
     * <p>An optional ID of place with same place ID already existing in DB.</p>
     */
    private Integer existingPlaceId;

    /**
     * <p>A mandatory place ID extracted from the Excel file.</p>
     */
    private String placeId;

    /**
     * <p>A mandatory place latitude coordinate extracted from the Excel file.</p>
     */
    private Double lat;

    /**
     * <p>A mandatory place longitude coordinate extracted from the Excel file.</p>
     */
    private Double lng;

    /**
     * <p>A mandatory place address extracted from the Excel file.</p>
     */
    private String address;

    /**
     * <p>A number of occurrences of preceding records with same place ID as this one in Excel file.</p>
     */
    private int count;

    /**
     * <p>Constructs new <code>PlaceImportStatusItem</code> instance. This implementation does nothing.</p>
     */
    public PlaceImportStatusItem() {
    }

    public Integer getExistingPlaceId() {
        return existingPlaceId;
    }

    public void setExistingPlaceId(Integer existingPlaceId) {
        this.existingPlaceId = existingPlaceId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "PlaceImportStatusItem{" +
                "existingPlaceId=" + existingPlaceId +
                ", placeId='" + placeId + '\'' +
                ", lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                ", address='" + address + '\'' +
                ", count=" + count +
                '}';
    }
}
