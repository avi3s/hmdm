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

package com.hmdm.plugins.devicereset.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.plugins.devicereset.persistence.domain.DeviceResetStatus;
import com.hmdm.plugins.devicereset.persistence.mapper.DeviceResetMapper;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>A DAO for {@link DeviceResetStatus} domain objects.</p>
 *
 * @author isv
 */
@Singleton
public class DeviceResetDAO {

    private static final Logger logger = LoggerFactory.getLogger(DeviceResetDAO.class);

    /**
     * <p>An interface to persistence layer.</p>
     */
    private final DeviceResetMapper deviceResetMapper;

    /**
     * <p>Constructs new <code>DeviceResetDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceResetDAO(DeviceResetMapper deviceResetMapper) {
        this.deviceResetMapper = deviceResetMapper;
    }

    /**
     * <p>Records the timestamp of requesting the resetting to factory settings for specified device.</p>
     *
     * @param deviceId an ID of a device.
     * @param ts a timestamp of request for device resetting (in milliseconds since epoch time).
     */
    @Transactional
    public void onDeviceResetRequest(int deviceId, long ts) {
        logger.debug("Recording the timestamp of requesting the resetting to factory settings for device #{} on {}",
                deviceId, new Date(ts));

        DeviceResetStatus status = this.deviceResetMapper.getDeviceResetStatus(deviceId);

        if (status == null) {
            status = new DeviceResetStatus();
            status.setDeviceId(deviceId);
        }
        status.setStatusResetRequested(ts);
        status.setStatusResetConfirmed(null);

        this.deviceResetMapper.insertDeviceResetStatus(status);
    }

    /**
     * <p>Records the timestamp of resetting to factory settings for specified device as reported by device.</p>
     *
     * @param deviceId an ID of a device.
     * @param ts a timestamp of resetting device to factory settings (in milliseconds since epoch time).
     */
    public void onDeviceResetConfirmation(int deviceId, long ts) {
        logger.debug("Recording the timestamp of confirming the resetting to factory settings by device #{} on {}",
                deviceId, new Date(ts));

        DeviceResetStatus status = this.deviceResetMapper.getDeviceResetStatus(deviceId);
        if (status != null) {
            status.setStatusResetConfirmed(ts);
            status.setRebootRequested(null);
            status.setRebootConfirmed(null);
            status.setDeviceLocked(false);
            status.setLockMessage(null);
            this.deviceResetMapper.insertDeviceResetStatus(status);
        }
    }

    /**
     * <p>Records the timestamp of requesting the reboot for specified device.</p>
     *
     * @param deviceId an ID of a device.
     * @param ts a timestamp of request for reboot (in milliseconds since epoch time).
     */
    @Transactional
    public void onDeviceRebootRequest(int deviceId, long ts) {
        logger.debug("Recording the timestamp of requesting the reboot for device #{} on {}",
                deviceId, new Date(ts));

        DeviceResetStatus status = this.deviceResetMapper.getDeviceResetStatus(deviceId);

        if (status == null) {
            status = new DeviceResetStatus();
            status.setDeviceId(deviceId);
        }
        status.setRebootRequested(ts);
        status.setRebootConfirmed(null);

        this.deviceResetMapper.insertDeviceResetStatus(status);
    }

    /**
     * <p>Records the timestamp of reboot for specified device as reported by device.</p>
     *
     * @param deviceId an ID of a device.
     * @param ts a timestamp of rebooting device (in milliseconds since epoch time).
     */
    public void onDeviceRebootConfirmation(int deviceId, long ts) {
        logger.debug("Recording the timestamp of confirming the reboot by device #{} on {}",
                deviceId, new Date(ts));

        DeviceResetStatus status = this.deviceResetMapper.getDeviceResetStatus(deviceId);
        if (status != null) {
            status.setRebootConfirmed(ts);
            this.deviceResetMapper.insertDeviceResetStatus(status);
        }
    }

    /**
     * <p>Locks or unlocks the specified device.</p>
     *
     * @param deviceId an ID of a device.
     * @param deviceLocked specifies whether the device should be locked
     */
    @Transactional
    public void lockDevice(int deviceId, boolean deviceLocked, String lockMessage) {
        logger.debug("Locking state for device #{}: {}", deviceId, deviceLocked);

        DeviceResetStatus status = this.deviceResetMapper.getDeviceResetStatus(deviceId);

        if (status == null) {
            status = new DeviceResetStatus();
            status.setDeviceId(deviceId);
        }
        status.setDeviceLocked(deviceLocked);
        status.setLockMessage(lockMessage);

        this.deviceResetMapper.insertDeviceResetStatus(status);
    }

    /**
     * <p>Resets the password of the device.</p>
     *
     * @param deviceId an ID of a device.
     * @param password a new password.
     */
    @Transactional
    public void resetPassword(int deviceId, String password) {
        logger.debug("Requesting the password reset for device #{}. New password: #{}", deviceId, password);

        DeviceResetStatus status = this.deviceResetMapper.getDeviceResetStatus(deviceId);

        if (status == null) {
            status = new DeviceResetStatus();
            status.setDeviceId(deviceId);
        }
        status.setPassword(password);

        this.deviceResetMapper.insertDeviceResetStatus(status);
    }

    /**
     * <p>Confirms the password reset.</p>
     *
     * @param deviceId an ID of a device.
     * @param ts a timestamp of resetting the password for the device (in milliseconds since epoch time).
     */
    public void onPasswordResetConfirmation(int deviceId, long ts) {
        logger.debug("Recording the timestamp of resetting password on device #{} on {}",
                deviceId, new Date(ts));

        DeviceResetStatus status = this.deviceResetMapper.getDeviceResetStatus(deviceId);
        if (status != null) {
            // Just clear the password to confirm the signal to password reset has been received
            status.setPassword(null);
            this.deviceResetMapper.insertDeviceResetStatus(status);
        }
    }

    /**
     * <p>Gets the current status of device reset to factory settings.</p>
     *
     * @param deviceId an ID of a device.
     * @return a current status for device reset request or <code>null</code> if there were no requests for resetting
     *         device issued before.
     */
    public DeviceResetStatus getDeviceResetStatus(int deviceId) {
        logger.debug("Getting current reset status for device #{}", deviceId);
        return this.deviceResetMapper.getDeviceResetStatus(deviceId);
    }

    /**
     * <p>Gets the current statuses of device reset to factory settings.</p>
     *
     * @param deviceIds a list of IDs of devices.
     * @return a list of current statuses for device reset request.
     */
    public List<DeviceResetStatus> getDeviceResetStatuses(List<Integer> deviceIds) {
        if (deviceIds == null || deviceIds.size() == 0) {
            return new LinkedList<DeviceResetStatus>();
        }
        logger.trace("Getting current reset status for devices: {}", deviceIds);
        return this.deviceResetMapper.getDeviceResetStatuses(deviceIds);
    }
}
