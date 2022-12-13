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

import java.io.Serializable;

/**
 * <p>A DTO carrying the results of importing devices from Excel file.</p>
 *
 * @author isv
 */
public class DeviceImportResult implements Serializable {

    private static final long serialVersionUID = 4272687901501161973L;

    /**
     * <p>A number of new device records added to DB.</p>
     */
    private int newDevicesCount;

    /**
     * <p>A number of existing device records updated in DB.</p>
     */
    private int existingDevicesUpdated;

    /**
     * <p>A number of existing device records skipped from updating in DB.</p>
     */
    private int existingDevicesSkipped;

    /**
     * <p>A number of skipped repeated device records from Excel file.</p>
     */
    private int repeatedRecordsSkipped;

    /**
     * <p>A number of skipped devices due to license limits</p>
     */
    private int devicesSkippedByLicense;

    /**
     * <p>Constructs new <code>DeviceImportResult</code> instance. This implementation does nothing.</p>
     */
    public DeviceImportResult() {
    }

    public int getNewDevicesCount() {
        return newDevicesCount;
    }

    public void setNewDevicesCount(int newDevicesCount) {
        this.newDevicesCount = newDevicesCount;
    }

    public int getExistingDevicesUpdated() {
        return existingDevicesUpdated;
    }

    public void setExistingDevicesUpdated(int existingDevicesUpdated) {
        this.existingDevicesUpdated = existingDevicesUpdated;
    }

    public int getExistingDevicesSkipped() {
        return existingDevicesSkipped;
    }

    public void setExistingDevicesSkipped(int existingDevicesSkipped) {
        this.existingDevicesSkipped = existingDevicesSkipped;
    }

    public int getRepeatedRecordsSkipped() {
        return repeatedRecordsSkipped;
    }

    public void setRepeatedRecordsSkipped(int repeatedRecordsSkipped) {
        this.repeatedRecordsSkipped = repeatedRecordsSkipped;
    }

    public int getDevicesSkippedByLicense() {
        return devicesSkippedByLicense;
    }

    public void setDevicesSkippedByLicense(int devicesSkippedByLicense) {
        this.devicesSkippedByLicense = devicesSkippedByLicense;
    }

    @Override
    public String toString() {
        return "DeviceImportResult{" +
                "newDevicesCount=" + newDevicesCount +
                ", existingDevicesUpdated=" + existingDevicesUpdated +
                ", existingDevicesSkipped=" + existingDevicesSkipped +
                ", repeatedRecordsSkipped=" + repeatedRecordsSkipped +
                ", devicesSkippedByLicense=" + devicesSkippedByLicense +
                '}';
    }
}
