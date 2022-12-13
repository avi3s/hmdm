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
 * <p>An exported form of the device.</p>
 *
 * @author isv
 */
public class DeviceExportRecord implements Serializable {

    private static final long serialVersionUID = 4579976413532485644L;

    /**
     * <p>An ID of a device.</p>
     */
    private int id;

    /**
     * <p>An unique number identifying the device among others.</p>
     */
    private String deviceNumber;

    /**
     * <p>An IMEI of device.</p>
     */
    private String imei;

    /**
     * <p>A serial number of device.</p>
     */
    private String serial;

    /**
     * <p>A phone number of device.</p>
     */
    private String phone;

    /**
     * <p>Device description.</p>
     */
    private String description;

    /**
     * <p>An ID of a configuration set for device.</p>
     */
    private int configurationId;

    /**
     * <p>A name of configuration set for device.</p>
     */
    private String configurationName;

    /**
     * <p>A version of the launcher application set for device configuration.</p>
     */
    private String launcherVersion;

    /**
     * <p>Timestamp of last synchronization.</p>
     */
    private long syncTime;

    /**
     * <p>A model of the device.</p>
     */
    private String model;

    /**
     * <p>Default launcher.</p>
     */
    private String launcher;

    /**
     * <p>Custom property #1.</p>
     */
    private String custom1;

    /**
     * <p>Custom property #2.</p>
     */
    private String custom2;

    /**
     * <p>Custom property #3.</p>
     */
    private String custom3;

    /**
     * <p>A flag indicating if launcher application is granted an administrator permission on device.</p>
     */
    private Boolean adminPermission;

    /**
     * <p>A flag indicating if launcher application is granted permission to overlap other applications on device.</p>
     */
    private Boolean overlapPermission;

    /**
     * <p>A flag indicating if launcher application is granted permission to access usage history on device.</p>
     */
    private Boolean historyPermission;

    /**
     * <p>A flag indicating if launcher application is granted device owner (MDM) permissions.</p>
     */
    private Boolean mdmMode;

    /**
     * <p>A flag indicating if launcher application is running kiosk mode.</p>
     */
    private Boolean kioskMode;

    /**
     * <p>Android version of the device.</p>
     */
    private String androidVersion;

    /**
     * <p>A list of applications set on application as reported by application.</p>
     */
    private List<DeviceExportApplicationDeviceView> applications;

    /**
     * <p>A flag indicating if device has synchronized data with server application at least once.</p>
     */
    private boolean infoAvailable;

    /**
     * <p>Constructs new <code>DeviceExportRecord</code> instance. This implementation does nothing.</p>
     */
    public DeviceExportRecord() {
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public String getLauncherVersion() {
        return launcherVersion;
    }

    public void setLauncherVersion(String launcherVersion) {
        this.launcherVersion = launcherVersion;
    }

    public long getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(long syncTime) {
        this.syncTime = syncTime;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLauncher() {
        return launcher;
    }

    public void setLauncher(String launcher) {
        this.launcher = launcher;
    }

    public String getCustom1() {
        return custom1;
    }

    public void setCustom1(String custom1) {
        this.custom1 = custom1;
    }

    public String getCustom2() {
        return custom2;
    }

    public void setCustom2(String custom2) {
        this.custom2 = custom2;
    }

    public String getCustom3() {
        return custom3;
    }

    public void setCustom3(String custom3) {
        this.custom3 = custom3;
    }

    public Boolean getAdminPermission() {
        return adminPermission;
    }

    public void setAdminPermission(Boolean adminPermission) {
        this.adminPermission = adminPermission;
    }

    public Boolean getOverlapPermission() {
        return overlapPermission;
    }

    public void setOverlapPermission(Boolean overlapPermission) {
        this.overlapPermission = overlapPermission;
    }

    public Boolean getHistoryPermission() {
        return historyPermission;
    }

    public void setHistoryPermission(Boolean historyPermission) {
        this.historyPermission = historyPermission;
    }

    public Boolean getMdmMode() {
        return mdmMode;
    }

    public void setMdmMode(Boolean mdmMode) {
        this.mdmMode = mdmMode;
    }

    public Boolean getKioskMode() {
        return kioskMode;
    }

    public void setKioskMode(Boolean kioskMode) {
        this.kioskMode = kioskMode;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public List<DeviceExportApplicationDeviceView> getApplications() {
        return applications;
    }

    public void setApplications(List<DeviceExportApplicationDeviceView> applications) {
        this.applications = applications;
    }

    public int getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(int configurationId) {
        this.configurationId = configurationId;
    }

    public boolean isInfoAvailable() {
        return infoAvailable;
    }

    public void setInfoAvailable(boolean infoAvailable) {
        this.infoAvailable = infoAvailable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "DeviceExportRecord{" +
                "deviceNumber='" + deviceNumber + '\'' +
                ", id='" + id + '\'' +
                ", imei='" + imei + '\'' +
                ", phone='" + phone + '\'' +
                ", configurationId='" + configurationId + '\'' +
                ", configurationName='" + configurationName + '\'' +
                ", launcherVersion='" + launcherVersion + '\'' +
                ", custom1='" + custom1 + '\'' +
                ", custom2='" + custom2 + '\'' +
                ", custom3='" + custom3 + '\'' +
                ", adminPermission=" + adminPermission +
                ", overlapPermission=" + overlapPermission +
                ", historyPermission=" + historyPermission +
                ", infoAvailable=" + infoAvailable +
                '}';
    }
}
