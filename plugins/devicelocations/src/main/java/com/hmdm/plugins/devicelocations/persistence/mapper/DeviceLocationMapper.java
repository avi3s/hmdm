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

package com.hmdm.plugins.devicelocations.persistence.mapper;

import com.hmdm.persistence.domain.Device;
import com.hmdm.plugins.devicelocations.persistence.domain.DeviceLocation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cursor.Cursor;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p>An ORM mapper for {@link DeviceLocation} domain objects.</p>
 *
 * @author isv
 */
public interface DeviceLocationMapper {

    @Select("SELECT loc.* " +
            "FROM plugin_devicelocations_latest loc " +
            "INNER JOIN devices ON devices.id = loc.deviceId " +
            "WHERE devices.id = #{deviceId} " +
            "AND devices.customerId = #{customerId} ")
    DeviceLocation getLatestDeviceLocation(@Param("deviceId") Integer deviceId,
                                           @Param("customerId") int customerId);

    @Insert("INSERT INTO plugin_devicelocations_latest " +
            "(deviceId, ts, lat, lon) " +
            "VALUES " +
            "(#{deviceId}, #{ts}, #{lat}, #{lon}) " +
            "ON CONFLICT ON CONSTRAINT plugin_devicelocations_latest_device_unique DO " +
            "UPDATE SET " +
            "ts = EXCLUDED.ts, " +
            "lat = EXCLUDED.lat, " +
            "lon = EXCLUDED.lon "
    )
    int saveDeviceLatestLocation(DeviceLocation deviceLocation);

    @Insert({"<script>",
            "INSERT INTO plugin_devicelocations_history " +
            "(deviceId, ts, lat, lon) " +
            "VALUES " +
            "<foreach item='location' collection='deviceLocations' separator=', '>" +
            "(#{location.deviceId}, #{location.ts}, #{location.lat}, #{location.lon})" +
            "</foreach>",
            "</script>"
    })
    int saveDeviceLocationHistory(@Param("deviceLocations") List<DeviceLocation> deviceLocations);

    /**
     * <p>Deletes the device info records which are older than number of days configured in customer's profile.</p>
     *
     * @return a number of deleted records.
     */
    @Delete("" +
            "DELETE " +
            "FROM plugin_devicelocations_history " +
            "WHERE ts < (SELECT EXTRACT(EPOCH FROM DATE_TRUNC('day', NOW() - (pds.dataPreservePeriod || ' day')::INTERVAL)) * 1000 " +
            "            FROM plugin_devicelocations_settings pds " +
            "            WHERE pds.customerId = (SELECT customerId FROM devices WHERE devices.id = plugin_devicelocations_history.deviceid)) " +
            "")
    int purgeDeviceLocationRecords();

    @Select({
            "<script>",
            "SELECT loc.* " +
                    "FROM plugin_devicelocations_latest loc " +
                    "INNER JOIN devices ON devices.id = loc.deviceId " +
                    "WHERE devices.id IN " +
                    "<foreach item='deviceId' collection='deviceIds' open='(' separator=', ' close=')'>" +
                    "#{deviceId}" +
                    "</foreach>" +
                    "AND devices.customerId = #{customerId} " +
                    "AND (loc.lat != 0 OR loc.lon != 0) " +
                    "ORDER BY loc.deviceId",
            "</script>"
    })
    List<DeviceLocation> getLatestDeviceLocations(@Param("deviceIds") List<Integer> deviceIds,
                                                  @Param("customerId") int customerId);

    @Select({
            "<script>",
            "SELECT id, number, imei, description " +
                    "FROM devices " +
                    "WHERE devices.id IN " +
                    "<foreach item='deviceId' collection='deviceIds' open='(' separator=', ' close=')'>" +
                    "#{deviceId}" +
                    "</foreach>" +
                    "AND devices.customerId = #{customerId}",
            "</script>"
    })
    List<Device> getDevices(@Param("deviceIds") List<Integer> deviceIds,
                            @Param("customerId") int customerId);

    @Select(
            "SELECT COUNT(history.id) " +
                    "FROM plugin_devicelocations_history history " +
                    "INNER JOIN devices ON devices.id = history.deviceId " +
                    "WHERE devices.id = #{deviceId} " +
                    "AND   devices.customerId = #{customerId} " +
                    "AND   history.ts BETWEEN #{dateFrom} AND #{dateTo} " +
                    "AND   (history.lat != 0 OR history.lon != 0) "
    )
    int countDeviceLocationHistoryRecords(
            @Param("deviceId") int deviceId,
            @Param("customerId") int customerId,
            @Param("dateFrom") long dateFrom,
            @Param("dateTo") long dateTo
    );

    @Select(
            "SELECT history.* " +
                    "FROM plugin_devicelocations_history history " +
                    "INNER JOIN devices ON devices.id = history.deviceId " +
                    "WHERE devices.id = #{deviceId} " +
                    "AND   devices.customerId = #{customerId} " +
                    "AND   history.ts BETWEEN #{dateFrom} AND #{dateTo} " +
                    "AND   (history.lat != 0 OR history.lon != 0) " +
                    "ORDER BY history.ts " +
                    "OFFSET  (#{pageNum} - 1) * #{pageSize} " +
                    "LIMIT #{pageSize}"
    )
    List<DeviceLocation> getDeviceLocationHistoryRecords(
            @Param("deviceId") int deviceId,
            @Param("customerId") int customerId,
            @Param("dateFrom") long dateFrom,
            @Param("dateTo") long dateTo,
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );

    @Select(
            "SELECT history.* " +
                    "FROM plugin_devicelocations_history history " +
                    "INNER JOIN devices ON devices.id = history.deviceId " +
                    "WHERE devices.id = #{deviceId} " +
                    "AND   devices.customerId = #{customerId} " +
                    "AND   history.ts BETWEEN #{dateFrom} AND #{dateTo} " +
                    "AND   (history.lat != 0 OR history.lon != 0) " +
                    "ORDER BY history.ts"
    )
    Cursor<DeviceLocation> getDeviceLocationHistoryRecordsForExport(
            @Param("deviceId") int deviceId,
            @Param("customerId") int customerId,
            @Param("dateFrom") long dateFrom,
            @Param("dateTo") long dateTo
    );
}
