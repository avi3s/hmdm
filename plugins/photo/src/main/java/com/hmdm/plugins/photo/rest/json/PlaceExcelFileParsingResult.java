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
import java.util.List;

/**
 * <p>A DTO carrying the details on parsing the Excel file with place details to be imported.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlaceExcelFileParsingResult implements Serializable {

    private static final long serialVersionUID = -4134264214396996473L;
    
    /**
     * <p>An ID identifying this result among others.</p>
     */
    private final String uuid;

    /**
     * <p>A list of place parsed from the Excel file.</p>
     */
    private final List<PlaceImportStatusItem> places;

    /**
     * <p>A number of new places to be added to DB as result of import.</p>
     */
    private int newRecordsCount;

    /**
     * <p>A number of place records with incomplete data.</p>
     */
    private int badRecordsCount;

    /**
     * <p>An estimated number of place records in DB after import.</p>
     */
    private int estimatedDBRecordsCount;

    /**
     * <p>A number of place records already existing in DB.</p>
     */
    private final int existingRecordsCount;

    /**
     * <p>A number of duplicate records in Excel file.</p>
     */
    private final int duplicateRecordsCount;

    /**
     * <p>Constructs new <code>PlaceExcelFileParsingResult</code> instance. This implementation does nothing.</p>
     */
    public PlaceExcelFileParsingResult(String uuid, List<PlaceImportStatusItem> places, int newRecordsCount,
                                       int badRecordsCount, int estimatedDBRecordsCount, int existingRecordsCount,
                                       int duplicateRecordsCount) {
        this.uuid = uuid;
        this.places = places;
        this.newRecordsCount = newRecordsCount;
        this.badRecordsCount = badRecordsCount;
        this.estimatedDBRecordsCount = estimatedDBRecordsCount;
        this.existingRecordsCount = existingRecordsCount;
        this.duplicateRecordsCount = duplicateRecordsCount;
    }

    public String getUuid() {
        return uuid;
    }

    public List<PlaceImportStatusItem> getPlaces() {
        return places;
    }

    public int getNewRecordsCount() {
        return newRecordsCount;
    }

    public void setNewRecordsCount(int newRecordsCount) {
        this.newRecordsCount = newRecordsCount;
    }

    public int getBadRecordsCount() {
        return badRecordsCount;
    }

    public void setBadRecordsCount(int badRecordsCount) {
        this.badRecordsCount = badRecordsCount;
    }

    public int getEstimatedDBRecordsCount() {
        return estimatedDBRecordsCount;
    }

    public void setEstimatedDBRecordsCount(int estimatedDBRecordsCount) {
        this.estimatedDBRecordsCount = estimatedDBRecordsCount;
    }

    public int getExistingRecordsCount() {
        return existingRecordsCount;
    }

    public int getDuplicateRecordsCount() {
        return duplicateRecordsCount;
    }

    @Override
    public String toString() {
        return "PlaceExcelFileParsingResult{" +
                "uuid='" + uuid + '\'' +
                ", places=" + places +
                ", newRecordsCount=" + newRecordsCount +
                ", badRecordsCount=" + badRecordsCount +
                ", existingRecordsCount=" + existingRecordsCount +
                ", duplicateRecordsCount=" + duplicateRecordsCount +
                ", estimatedDBRecordsCount=" + estimatedDBRecordsCount +
                '}';
    }
}
