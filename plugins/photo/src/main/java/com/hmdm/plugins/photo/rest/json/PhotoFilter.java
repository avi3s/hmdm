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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * <p>A DTO carrying the parameters for filtering the lists of photo objects.</p>
 *
 * @author isv
 */
@ApiModel(description = "The parameters for filtering the lists of photo objects")
@JsonIgnoreProperties(value = {"featurePlacesEnabled", "linkPhotoToPlace"}, ignoreUnknown = true)
public class PhotoFilter {

    /**
     * <p>A number of records per single page of data to be retrieved.</p>
     */
    @ApiModelProperty("A number of records per single page of data to be retrieved")
    private int pageSize = 50;

    /**
     * <p>A number of page of data to be retrieved.</p>
     */
    @ApiModelProperty("A number of page of data to be retrieved (1-based)")
    private int pageNum = 1;

    /**
     * <p>A filter used for filtering the data records by device.</p>
     */
    @ApiModelProperty("A filter used for filtering the data records by device")
    private String deviceFilter;

    /**
     * <p>A filter used for filtering the data records by address.</p>
     */
    @ApiModelProperty("A filter used for filtering the data records by address")
    private String addressFilter;

    /**
     * <p>A timestamp for <code>FROM</code> boundary for filtering the data records by dates.</p>
     */
    @ApiModelProperty("A timestamp for FROM boundary for filtering the data records by dates")
    private Date dateFrom;

    /**
     * <p>A timestamp for <code>TO</code> boundary for filtering the data records by dates.</p>
     */
    @ApiModelProperty("A timestamp for TO boundary for filtering the data records by dates")
    private Date dateTo;

    /**
     * <p>An ID of a customer.</p>
     */
    @ApiModelProperty(hidden = true)
    private int customerId;

    /**
     * <p>A name of sorting column.</p>
     */
    @ApiModelProperty("A name of sorting column")
    private String sortValue = "createTime";

    /**
     * <p>An ID of a user.</p>
     */
    @ApiModelProperty(hidden = true)
    private int userId;

    /**
     * <p>A filter used for filtering the data records by related point.</p>
     */
    @ApiModelProperty("A filter used for filtering the data records by related point")
    private String pointFilter;

    /**
     * <p>A flag indicating if <code>PLaces</code> feature is enabled.</p>
     */
    @ApiModelProperty(hidden = true)
    private boolean featurePlacesEnabled;

    /**
     * <p>A flag indicating if photos are to be linked to places.</p>
     */
    @ApiModelProperty(hidden = true)
    private boolean linkPhotoToPlace;

    /**
     * <p>Constructs new <code>PhotoFilter</code> instance. This implementation does nothing.</p>
     */
    public PhotoFilter() {
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public String getDeviceFilter() {
        return deviceFilter;
    }

    public void setDeviceFilter(String filter) {
        this.deviceFilter = filter;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        if (dateTo != null) {
            dateTo.setTime(dateTo.getTime() + (23 * 3600 + 59 * 60 + 59) * 1000);
        }
        this.dateTo = dateTo;
    }

    public String getSortValue() {
        return sortValue;
    }

    public void setSortValue(String sortValue) {
        this.sortValue = sortValue;
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

    public String getAddressFilter() {
        return addressFilter;
    }

    public void setAddressFilter(String addressFilter) {
        this.addressFilter = addressFilter;
    }

    public String getPointFilter() {
        return pointFilter;
    }

    public void setPointFilter(String pointFilter) {
        this.pointFilter = pointFilter;
    }

    public boolean isFeaturePlacesEnabled() {
        return featurePlacesEnabled;
    }

    public void setFeaturePlacesEnabled(boolean featurePlacesEnabled) {
        this.featurePlacesEnabled = featurePlacesEnabled;
    }

    public boolean isLinkPhotoToPlace() {
        return linkPhotoToPlace;
    }

    public void setLinkPhotoToPlace(boolean linkPhotoToPlace) {
        this.linkPhotoToPlace = linkPhotoToPlace;
    }

    @Override
    public String toString() {
        return "PhotoFilter{" +
                "pageSize=" + pageSize +
                ", pageNum=" + pageNum +
                ", deviceFilter='" + deviceFilter + '\'' +
                ", pointFilter='" + pointFilter + '\'' +
                ", addressFilter='" + addressFilter + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", customerId=" + customerId +
                ", userId=" + userId +
                ", sortValue='" + sortValue + '\'' +
                '}';
    }
}
