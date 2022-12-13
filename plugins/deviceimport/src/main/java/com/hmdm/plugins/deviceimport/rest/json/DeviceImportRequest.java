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

import java.io.Serializable;

/**
 * <p>A DTO carrying the details for request for importing the devices from the uploaded Excel file.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceImportRequest implements Serializable {

    private static final long serialVersionUID = 2439638793832182934L;

    /**
     * <p>An optional ID of a group to be assigned to imported devices.</p>
     */
    private Integer groupId;

    /**
     * <p>A mandatory ID of a configuration to be assigned to imported devices.</p>
     */
    private int configurationId;

    /**
     * <p>A mandatory index for column in Excel file to retrieve the device number from. (1-based)</p>
     */
    private int deviceNumberColumnIndex;

    /**
     * <p>An optional index for column in Excel file to retrieve the device IMEI from. (1-based)</p>
     */
    private Integer imeiColumnIndex;

    /**
     * <p>An optional index for column in Excel file to retrieve the device phone number from. (1-based)</p>
     */
    private Integer phoneNumberColumnIndex;

    /**
     * <p>An optional index for column in Excel file to retrieve the device description from. (1-based)</p>
     */
    private Integer descriptionColumnIndex;

    /**
     * <p>An optional index for column in Excel file to retrieve the custom property #1. (1-based)</p>
     */
    private Integer custom1ColumnIndex;

    /**
     * <p>An optional index for column in Excel file to retrieve the custom property #2. (1-based)</p>
     */
    private Integer custom2ColumnIndex;

    /**
     * <p>An optional index for column in Excel file to retrieve the custom property #3. (1-based)</p>
     */
    private Integer custom3ColumnIndex;


    /**
     * <p>An ID referencing the uploaded Excel file to import devices from.</p>
     */
    private String filePathId;

    /**
     * <p>A type of the import.</p>
     */
    private ImportType importType;

    /**
     * <p>A content of device list when import type is {@link ImportType#LIST}.</p>
     */
    private String listContent;

    /**
     * <p>Constructs new <code>DeviceImportRequest</code> instance. This implementation does nothing.</p>
     */
    public DeviceImportRequest() {
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public int getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(int configurationId) {
        this.configurationId = configurationId;
    }

    public int getDeviceNumberColumnIndex() {
        return deviceNumberColumnIndex;
    }

    public void setDeviceNumberColumnIndex(int deviceNumberColumnIndex) {
        this.deviceNumberColumnIndex = deviceNumberColumnIndex;
    }

    public Integer getImeiColumnIndex() {
        return imeiColumnIndex;
    }

    public void setImeiColumnIndex(Integer imeiColumnIndex) {
        this.imeiColumnIndex = imeiColumnIndex;
    }

    public Integer getPhoneNumberColumnIndex() {
        return phoneNumberColumnIndex;
    }

    public void setPhoneNumberColumnIndex(Integer phoneNumberColumnIndex) {
        this.phoneNumberColumnIndex = phoneNumberColumnIndex;
    }

    public Integer getDescriptionColumnIndex() {
        return descriptionColumnIndex;
    }

    public void setDescriptionColumnIndex(Integer descriptionColumnIndex) {
        this.descriptionColumnIndex = descriptionColumnIndex;
    }

    public Integer getCustom1ColumnIndex() {
        return custom1ColumnIndex;
    }

    public void setCustom1ColumnIndex(Integer custom1ColumnIndex) {
        this.custom1ColumnIndex = custom1ColumnIndex;
    }

    public Integer getCustom2ColumnIndex() {
        return custom2ColumnIndex;
    }

    public void setCustom2ColumnIndex(Integer custom2ColumnIndex) {
        this.custom2ColumnIndex = custom2ColumnIndex;
    }

    public Integer getCustom3ColumnIndex() {
        return custom3ColumnIndex;
    }

    public void setCustom3ColumnIndex(Integer custom3ColumnIndex) {
        this.custom3ColumnIndex = custom3ColumnIndex;
    }

    public String getFilePathId() {
        return filePathId;
    }

    public void setFilePathId(String filePathId) {
        this.filePathId = filePathId;
    }

    public ImportType getImportType() {
        return importType;
    }

    public void setImportType(ImportType importType) {
        this.importType = importType;
    }

    public String getListContent() {
        return listContent;
    }

    public void setListContent(String listContent) {
        this.listContent = listContent;
    }

    @Override
    public String toString() {
        return "DeviceImportRequest{" +
                "groupId=" + groupId +
                ", configurationId=" + configurationId +
                ", deviceNumberColumnIndex=" + deviceNumberColumnIndex +
                ", imeiColumnIndex=" + imeiColumnIndex +
                ", phoneNumberColumnIndex=" + phoneNumberColumnIndex +
                ", descriptionColumnIndex=" + descriptionColumnIndex +
                ", custom1ColumnIndex=" + custom1ColumnIndex +
                ", custom2ColumnIndex=" + custom2ColumnIndex +
                ", custom3ColumnIndex=" + custom3ColumnIndex +
                ", filePathId='" + filePathId + '\'' +
                ", importType='" + importType + '\'' +
                ", listContent='" + (listContent != null) + '\'' +
                '}';
    }
}
