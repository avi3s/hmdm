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

package com.hmdm.plugins.deviceexport.persistence.domain;

import java.io.Serializable;

/**
 * <p>A device application object view suitable for purposes of this plugin.</p>
 *
 * @author isv
 */
public class DeviceExportApplicationDeviceView implements Serializable {

    private static final long serialVersionUID = 8915334115442676845L;

    private int deviceId;

    /**
     * <p>A package ID of application.</p>
     */
    private String pkg;

    /**
     * <p>A version of the application.</p>
     */
    private String version;

    /**
     * <p>Constructs new <code>DeviceExportApplicationDeviceView</code> instance. This implementation does nothing.</p>
     */
    public DeviceExportApplicationDeviceView() {
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "DeviceExportApplicationDeviceView{" +
                "deviceId='" + deviceId + '\'' +
                ", pkg='" + pkg + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    
}
