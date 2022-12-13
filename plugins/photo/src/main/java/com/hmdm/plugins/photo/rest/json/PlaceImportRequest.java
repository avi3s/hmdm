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

import java.io.Serializable;

/**
 * <p>A DTO carrying the details for request for importing the places from the uploaded Excel file.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaceImportRequest implements Serializable {

    private static final long serialVersionUID = -205712509507112705L;
    /**
     * <p>A mandatory index for column in Excel file to retrieve the place ID from. (1-based)</p>
     */
    private int placeIdColumnIndex;

    /**
     * <p>A mandatory index for column in Excel file to retrieve the place latitude coordinate from. (1-based)</p>
     */
    private int latColumnIndex;

    /**
     * <p>A mandatory index for column in Excel file to retrieve the place longitude coordinate from. (1-based)</p>
     */
    private int lngColumnIndex;

    /**
     * <p>A mandatory index for column in Excel file to retrieve the place address from. (1-based)</p>
     */
    private int addressColumnIndex;

    /**
     * <p>An ID referencing the uploaded Excel file to import places from.</p>
     */
    private String filePathId;

    /**
     * <p>Constructs new <code>PlaceImportRequest</code> instance. This implementation does nothing.</p>
     */
    public PlaceImportRequest() {
    }

    public int getPlaceIdColumnIndex() {
        return placeIdColumnIndex;
    }

    public void setPlaceIdColumnIndex(int placeIdColumnIndex) {
        this.placeIdColumnIndex = placeIdColumnIndex;
    }

    public int getLatColumnIndex() {
        return latColumnIndex;
    }

    public void setLatColumnIndex(int latColumnIndex) {
        this.latColumnIndex = latColumnIndex;
    }

    public int getLngColumnIndex() {
        return lngColumnIndex;
    }

    public void setLngColumnIndex(int lngColumnIndex) {
        this.lngColumnIndex = lngColumnIndex;
    }

    public int getAddressColumnIndex() {
        return addressColumnIndex;
    }

    public void setAddressColumnIndex(int addressColumnIndex) {
        this.addressColumnIndex = addressColumnIndex;
    }

    public String getFilePathId() {
        return filePathId;
    }

    public void setFilePathId(String filePathId) {
        this.filePathId = filePathId;
    }

    @Override
    public String toString() {
        return "PlaceImportRequest{" +
                "placeIdColumnIndex=" + placeIdColumnIndex +
                ", latColumnIndex=" + latColumnIndex +
                ", lngColumnIndex=" + lngColumnIndex +
                ", addressColumnIndex=" + addressColumnIndex +
                ", filePathId='" + filePathId + '\'' +
                '}';
    }
}
