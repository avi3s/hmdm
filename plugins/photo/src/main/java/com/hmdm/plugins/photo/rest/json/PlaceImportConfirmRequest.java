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
 * <p>A DTO carrying the parameters for confirmed place import process.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlaceImportConfirmRequest implements Serializable {

    private static final long serialVersionUID = 2613572550395261044L;
    
    /**
     * <p>An UUID referencing the results of previous parsing of Excel file.</p>
     */
    private String uuid;

    /**
     * <p>A flag indicating if existing devices are to be re-written (if set to 1) or left intact (if set to 2).</p>
     */
    private int existingMode;

    /**
     * <p>A flag indicating if all existing place records must be removed from database before importing new ones or
     * not.</p>
     */
    private boolean deleteExisting;

    /**
     * <p>Constructs new <code>PlaceImportConfirmRequest</code> instance. This implementation does nothing.</p>
     */
    public PlaceImportConfirmRequest() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getExistingMode() {
        return existingMode;
    }

    public void setExistingMode(int existingMode) {
        this.existingMode = existingMode;
    }

    public boolean isDeleteExisting() {
        return deleteExisting;
    }

    public void setDeleteExisting(boolean deleteExisting) {
        this.deleteExisting = deleteExisting;
    }

    @Override
    public String toString() {
        return "PlaceImportConfirmRequest{" +
                "uuid='" + uuid + '\'' +
                ", existingMode=" + existingMode +
                ", deleteExisting=" + deleteExisting +
                '}';
    }

}
