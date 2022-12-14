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

package com.hmdm.plugins.devicelocations.rest.json;

import java.io.Serializable;

/**
 * <p>$</p>
 *
 * @author isv
 */
public class DeviceLocationHistoryExportFilter implements Serializable {

    private static final long serialVersionUID = -6820949298792207766L;
    
    /**
     * <p>A locale used for localizing the generated content.</p>
     */
    private String locale;


    private Integer deviceId;

    private Long dateFrom;

    private Long dateTo;

    /**
     * <p>Constructs new <code>DeviceLocationHistoryExportFilter</code> instance. This implementation does nothing.</p>
     */
    public DeviceLocationHistoryExportFilter() {
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Long getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Long dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Long getDateTo() {
        return dateTo;
    }

    public void setDateTo(Long dateTo) {
        this.dateTo = dateTo;
    }

    @Override
    public String toString() {
        return "DeviceLocationHistoryExportFilter{" +
                "locale='" + locale + '\'' +
                ", deviceId=" + deviceId +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                '}';
    }
}
