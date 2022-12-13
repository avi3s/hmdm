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

package com.hmdm.plugins.devicereset.rest.json;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.rest.json.SyncApplicationInt;
import com.hmdm.rest.json.SyncApplicationSettingInt;
import com.hmdm.rest.json.SyncConfigurationFileInt;
import com.hmdm.rest.json.SyncResponseInt;

import java.io.Serializable;
import java.util.List;

/**
 * <p>A wrapper around the @link SyncResponseInt} to extend the wrapped object with <code>factoryReset</code> property.
 * </p>
 *
 * @author isv
 */
class DeviceResetSyncResponse implements SyncResponseInt, Serializable {

    private static final long serialVersionUID = 7122574438455960809L;

    @JsonIgnore
    private final SyncResponseInt wrapped;

    /**
     * <p>A flag indicating if device must be reset to factory settings.</p>
     */
    private Boolean factoryReset;

    /**
     * <p>A flag indicating if device must be rebooted.</p>
     */
    private Boolean reboot;

    /**
     * <p>A flag indicating if device must be locked.</p>
     */
    private Boolean lock;

    /**
     * <p>A message shown on a locked device.</p>
     */
    private String lockMessage;

    /**
     * <p>If password needs to be reset, this value is not null.</p>
     */
    private String passwordReset;

    /**
     * <p>Constructs new <code>DeviceResetSyncResponse</code> instance. This implementation does nothing.</p>
     */
    DeviceResetSyncResponse(SyncResponseInt wrapped) {
        this.wrapped = wrapped;
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getBackgroundColor() {
        return this.wrapped.getBackgroundColor();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getTextColor() {
        return this.wrapped.getTextColor();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getBackgroundImageUrl() {
        return this.wrapped.getBackgroundImageUrl();
    }

    @JsonGetter
    @Override
    public List<SyncApplicationInt> getApplications() {
        return this.wrapped.getApplications();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPassword() {
        return this.wrapped.getPassword();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getImei() {
        return this.wrapped.getImei();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPhone() {
        return this.wrapped.getPhone();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getIconSize() {
        return this.wrapped.getIconSize();
    }

    @JsonGetter
    @Override
    public String getTitle() {
        return this.wrapped.getTitle();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getGps() {
        return this.wrapped.getGps();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getBluetooth() {
        return this.wrapped.getBluetooth();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getWifi() {
        return this.wrapped.getWifi();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getMobileData() {
        return this.wrapped.getMobileData();
    }

    @JsonGetter
    @Override
    public boolean isKioskMode() {
        return this.wrapped.isKioskMode();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getKioskHome() {
        return this.wrapped.getKioskHome();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getKioskRecents() {
        return this.wrapped.getKioskRecents();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getKioskNotifications() {
        return this.wrapped.getKioskNotifications();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getKioskSystemInfo() {
        return this.wrapped.getKioskSystemInfo();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getKioskKeyguard() {
        return this.wrapped.getKioskKeyguard();
    }

    @JsonGetter
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getKioskLockButtons() {
        return this.wrapped.getKioskLockButtons();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public String getMainApp() {
        return this.wrapped.getMainApp();
    }

    @JsonGetter
    @Override
    public boolean isLockStatusBar() {
        return this.wrapped.isLockStatusBar();
    }

    @JsonGetter
    @Override
    public int getSystemUpdateType() {
        return this.wrapped.getSystemUpdateType();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public String getSystemUpdateFrom() {
        return this.wrapped.getSystemUpdateFrom();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public String getSystemUpdateTo() {
        return this.wrapped.getSystemUpdateTo();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public Boolean getScheduleAppUpdate() {
        return this.wrapped.getScheduleAppUpdate();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public String getAppUpdateFrom() {
        return this.wrapped.getAppUpdateFrom();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public String getAppUpdateTo() {
        return this.wrapped.getAppUpdateTo();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public List<SyncApplicationSettingInt> getApplicationSettings() {
        return this.wrapped.getApplicationSettings();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getFactoryReset() {
        return factoryReset;
    }

    public void setFactoryReset(Boolean factoryReset) {
        this.factoryReset = factoryReset;
    }


    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getUsbStorage() {
        return this.wrapped.getUsbStorage();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getRequestUpdates() {
        return this.wrapped.getRequestUpdates();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getDisableLocation() {
        return this.wrapped.getDisableLocation();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getAppPermissions() {
        return this.wrapped.getAppPermissions();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPushOptions() {
        return this.wrapped.getPushOptions();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getKeepaliveTime() {
        return this.wrapped.getKeepaliveTime();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getAutoBrightness() {
        return this.wrapped.getAutoBrightness();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getBrightness() {
        return this.wrapped.getBrightness();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getManageTimeout() {
        return this.wrapped.getManageTimeout();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getTimeout() {
        return this.wrapped.getTimeout();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getLockVolume() {
        return this.wrapped.getLockVolume();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getManageVolume() {
        return this.wrapped.getManageVolume();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getVolume() {
        return this.wrapped.getVolume();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPasswordMode() {
        return this.wrapped.getPasswordMode();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getOrientation() {
        return this.wrapped.getOrientation();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getRunDefaultLauncher() {
        return this.wrapped.getRunDefaultLauncher();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getDisableScreenshots() {
        return this.wrapped.getDisableScreenshots();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getTimeZone() {
        return this.wrapped.getTimeZone();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getAllowedClasses() {
        return this.wrapped.getAllowedClasses();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getNewServerUrl() {
        return this.wrapped.getNewServerUrl();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getLockSafeSettings() {
        return this.wrapped.getLockSafeSettings();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getShowWifi() {
        return this.wrapped.getShowWifi();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getReboot() {
        return reboot;
    }

    public void setReboot(Boolean reboot) {
        this.reboot = reboot;
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getLock() {
        return lock;
    }

    public void setLock(Boolean lock) {
        this.lock = lock;
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getLockMessage() {
        return lockMessage;
    }

    public void setLockMessage(String lockMessage) {
        this.lockMessage = lockMessage;
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPasswordReset() {
        return passwordReset;
    }

    public void setPasswordReset(String passwordReset) {
        this.passwordReset = passwordReset;
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public List<SyncConfigurationFileInt> getFiles() {
        return this.wrapped.getFiles();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public String getNewNumber() {
        return this.wrapped.getNewNumber();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public String getRestrictions() {
        return this.wrapped.getRestrictions();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public String getCustom1() {
        return this.wrapped.getCustom1();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public String getCustom2() {
        return this.wrapped.getCustom2();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public String getCustom3() {
        return this.wrapped.getCustom3();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public String getAppName() {
        return this.wrapped.getAppName();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public String getVendor() {
        return this.wrapped.getVendor();
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public String getDescription() {
        return this.wrapped.getDescription();
    }

}
