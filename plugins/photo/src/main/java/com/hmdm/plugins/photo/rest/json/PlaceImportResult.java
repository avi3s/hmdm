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

import java.io.Serializable;

/**
 * <p>A DTO carrying the results of importing places from Excel file.</p>
 *
 * @author isv
 */
public class PlaceImportResult implements Serializable {

    private static final long serialVersionUID = 1705999438914534353L;
    
    /**
     * <p>A number of new place records added to DB.</p>
     */
    private int newPlacesCount;

    /**
     * <p>A number of existing place records updated in DB.</p>
     */
    private int existingPlacesUpdated;

    /**
     * <p>A number of existing place records skipped from updating in DB.</p>
     */
    private int existingPlacesSkipped;

    /**
     * <p>A number of skipped repeated place records from Excel file.</p>
     */
    private int repeatedRecordsSkipped;


    /**
     * <p>Constructs new <code>PlaceImportResult</code> instance. This implementation does nothing.</p>
     */
    public PlaceImportResult() {
    }

    public int getNewPlacesCount() {
        return newPlacesCount;
    }

    public void setNewPlacesCount(int newPlacesCount) {
        this.newPlacesCount = newPlacesCount;
    }

    public int getExistingPlacesUpdated() {
        return existingPlacesUpdated;
    }

    public void setExistingPlacesUpdated(int existingPlacesUpdated) {
        this.existingPlacesUpdated = existingPlacesUpdated;
    }

    public int getExistingPlacesSkipped() {
        return existingPlacesSkipped;
    }

    public void setExistingPlacesSkipped(int existingPlacesSkipped) {
        this.existingPlacesSkipped = existingPlacesSkipped;
    }

    public int getRepeatedRecordsSkipped() {
        return repeatedRecordsSkipped;
    }

    public void setRepeatedRecordsSkipped(int repeatedRecordsSkipped) {
        this.repeatedRecordsSkipped = repeatedRecordsSkipped;
    }

    @Override
    public String toString() {
        return "PlaceImportResult{" +
                "newPlacesCount=" + newPlacesCount +
                ", existingPlacesUpdated=" + existingPlacesUpdated +
                ", existingPlacesSkipped=" + existingPlacesSkipped +
                ", repeatedRecordsSkipped=" + repeatedRecordsSkipped +
                '}';
    }
}
