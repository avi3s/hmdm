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
import java.util.List;

/**
 * <p>A configuration object view suitable for purposes of this plugin.</p>
 *
 * @author isv
 */
public class DeviceExportConfigurationView implements Serializable {

    private static final long serialVersionUID = 1859622840355746191L;
    
    /**
     * <p>An ID of a configuration.</p>
     */
    private int id;

    /**
     * <p>A list of applications set for configuration.</p>
     */
    private List<DeviceExportApplicationConfigurationView> applications;

    /**
     * <p>Constructs new <code>DeviceExportConfigurationView</code> instance. This implementation does nothing.</p>
     */
    public DeviceExportConfigurationView() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<DeviceExportApplicationConfigurationView> getApplications() {
        return applications;
    }

    public void setApplications(List<DeviceExportApplicationConfigurationView> applications) {
        this.applications = applications;
    }

    @Override
    public String toString() {
        return "DeviceExportConfigurationView{" +
                "id=" + id +
                ", applications=" + applications +
                '}';
    }
}
