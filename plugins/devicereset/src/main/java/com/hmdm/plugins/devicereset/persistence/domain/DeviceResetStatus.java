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

package com.hmdm.plugins.devicereset.persistence.domain;

import java.io.Serializable;

/**
 * <p>A domain object representing the current status of device reset to factory settings.</p>
 *
 * @author isv
 */
public class DeviceResetStatus implements Serializable {

    private static final long serialVersionUID = 8841182825864477636L;
    /**
     * <p>An ID of this record.</p>
     */
    private Integer id;

    /**
     * <p>An ID of a device which this info belongs to.</p>
     */
    private int deviceId;

    /**
     * <p>A timestamp of request for resetting the device (in milliseconds since epoch time).</p>
     */
    private Long statusResetRequested;

    /**
     * <p>A timestamp of resetting the device to factory settings as reported by device (in milliseconds since epoch
     * time).</p>
     */
    private Long statusResetConfirmed;


    /**
     * <p>A timestamp of request for rebooting the device (in milliseconds since epoch time).</p>
     */
    private Long rebootRequested;

    /**
     * <p>A timestamp of reboot on request as reported by device (in milliseconds since epoch
     * time).</p>
     */
    private Long rebootConfirmed;

    /**
     * <p>Lock device state.</p>
     */
    private boolean deviceLocked;

    /**
     * <p>Lock device message.</p>
     */
    private String lockMessage;

    /**
     * <p>Password if need to be reset on the device.</p>
     */
    private String password;

    /**
     * <p>Constructs new <code>DeviceResetStatus</code> instance. This implementation does nothing.</p>
     */
    public DeviceResetStatus() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public Long getStatusResetRequested() {
        return statusResetRequested;
    }

    public void setStatusResetRequested(Long statusResetRequested) {
        this.statusResetRequested = statusResetRequested;
    }

    public Long getStatusResetConfirmed() {
        return statusResetConfirmed;
    }

    public void setStatusResetConfirmed(Long statusResetConfirmed) {
        this.statusResetConfirmed = statusResetConfirmed;
    }

    public Long getRebootRequested() {
        return rebootRequested;
    }

    public void setRebootRequested(Long rebootRequested) {
        this.rebootRequested = rebootRequested;
    }

    public Long getRebootConfirmed() {
        return rebootConfirmed;
    }

    public void setRebootConfirmed(Long rebootConfirmed) {
        this.rebootConfirmed = rebootConfirmed;
    }

    public boolean isDeviceLocked() {
        return deviceLocked;
    }

    public void setDeviceLocked(boolean deviceLocked) {
        this.deviceLocked = deviceLocked;
    }

    public String getLockMessage() {
        return lockMessage;
    }

    public void setLockMessage(String lockMessage) {
        this.lockMessage = lockMessage;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "DeviceResetStatus{" +
                "id=" + id +
                ", deviceId=" + deviceId +
                ", statusResetRequested=" + statusResetRequested +
                ", statusResetConfirmed=" + statusResetConfirmed +
                ", rebootRequested=" + rebootRequested +
                ", rebootConfirmed=" + rebootConfirmed +
                ", deviceLocked=" + deviceLocked +
                ", lockMessage=" + lockMessage +
                ", password=" + password +
                '}';
    }
}
