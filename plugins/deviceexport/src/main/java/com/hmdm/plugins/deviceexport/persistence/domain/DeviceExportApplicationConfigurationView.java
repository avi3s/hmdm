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
 * <p>A configuration application object view suitable for purposes of this plugin.</p>
 *
 * @author isv
 */
public class DeviceExportApplicationConfigurationView implements Serializable {

    private static final long serialVersionUID = 3949515655615535912L;

    /**
     * <p>A type of the application</p>
     */
    private String type;

    /**
     * <p>A package ID of application.</p>
     */
    private String pkg;

    /**
     * <p>A version of the application.</p>
     */
    private String version;

    /**
     * <p>A name of application.</p>
     */
    private String name;

    /**
     * <p>An URL referencing the APK-file for application.</p>
     */
    private String url;

    /**
     * <p>An action to be performed with application on device side.</p>
     */
    private int action;

    /**
     * <p>Constructs new <code>DeviceExportApplicationView</code> instance. This implementation does nothing.</p>
     */
    public DeviceExportApplicationConfigurationView() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DeviceExportApplicationView{" +
                "pkg='" + pkg + '\'' +
                ", type='" + type + '\'' +
                ", version='" + version + '\'' +
                ", url='" + url + '\'' +
                ", action='" + action + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
