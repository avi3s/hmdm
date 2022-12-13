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

package com.hmdm.plugins.photo.persistence.domain;

import com.hmdm.persistence.domain.CustomerData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * <p>A domain object representing a single place on the map.</p>
 *
 * @author isv
 */
@ApiModel(description = "A place with known location")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Place implements Serializable, CustomerData {

    private static final long serialVersionUID = -29631598193326120L;
    /**
     * <p>An ID of a place.</p>
     */
    @ApiModelProperty("An ID of a place")
    private Integer id;

    /**
     * <p>An ID of a customer account which the record belongs to.</p>
     */
    @ApiModelProperty(hidden = true)
    private int customerId;

    /**
     * <p>A logical ID of a place.</p>
     */
    @ApiModelProperty("A logical ID of a place")
    private String placeId;

    /**
     * <p>A latitude coordinate of a place location.</p>
     */
    @ApiModelProperty("A latitude coordinate of a place location")
    private Double lat;

    /**
     * <p>A longitude coordinate of a place location.</p>
     */
    @ApiModelProperty("A longitude coordinate of a place location")
    private Double lng;

    /**
     * <p>An address of a place location.</p>
     */
    @ApiModelProperty("An address of a place location")
    private String address;

    /**
     * <p>A reserved field.</p>
     */
    @ApiModelProperty(hidden = true)
    private String reserve;

    /**
     * <p>Constructs new <code>Place</code> instance. This implementation does nothing.</p>
     */
    public Place() {
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

    public String getReserve() {
        return reserve;
    }

    public void setReserve(String reserve) {
        this.reserve = reserve;
    }

    @Override
    public String toString() {
        return "Place{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", placeId='" + placeId + '\'' +
                ", latitude=" + lat +
                ", longitude=" + lng +
                ", address='" + address + '\'' +
                ", reserve='" + reserve + '\'' +
                '}';
    }
}
