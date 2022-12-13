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

package com.hmdm.plugins.devicereset.persistence.mapper;

import com.hmdm.plugins.devicereset.persistence.domain.DeviceResetStatus;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>An ORM mapper for {@link DeviceResetStatus} domain objects.</p>
 *
 * @author isv
 */
public interface DeviceResetMapper {

    @Insert("INSERT INTO plugin_devicereset_status " +
            "(deviceId, statusResetRequested, statusResetConfirmed, rebootRequested, rebootConfirmed, deviceLocked, lockMessage, password) " +
            "VALUES " +
            "(#{deviceId}, #{statusResetRequested}, #{statusResetConfirmed}, " +
            " #{rebootRequested}, #{rebootConfirmed}, #{deviceLocked}, #{lockMessage}, #{password}) " +
            "ON CONFLICT ON CONSTRAINT plugin_devicereset_status_device_unique DO " +
            "UPDATE SET " +
            "statusResetRequested = EXCLUDED.statusResetRequested, " +
            "statusResetConfirmed = EXCLUDED.statusResetConfirmed, " +
            "rebootRequested = EXCLUDED.rebootRequested, " +
            "rebootConfirmed = EXCLUDED.rebootConfirmed, " +
            "deviceLocked = EXCLUDED.deviceLocked, " +
            "lockMessage = EXCLUDED.lockMessage, " +
            "password = EXCLUDED.password "
    )
    int insertDeviceResetStatus(DeviceResetStatus status);

    @Select("SELECT * FROM plugin_devicereset_status WHERE deviceId = #{deviceId} LIMIT 1")
    DeviceResetStatus getDeviceResetStatus(@Param("deviceId") int deviceId);

    List<DeviceResetStatus> getDeviceResetStatuses(@Param("deviceIds") List<Integer> deviceIds);
}
