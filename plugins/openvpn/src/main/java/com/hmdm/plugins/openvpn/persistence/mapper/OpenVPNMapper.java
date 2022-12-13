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

package com.hmdm.plugins.openvpn.persistence.mapper;
import com.hmdm.plugins.openvpn.persistence.domain.OpenVPNPluginDefaults;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

/**
 * <p>An ORM mapper for {@link com.hmdm.plugins.openvpn.persistence.domain.OpenVPNPluginDefaults} domain object.</p>
 *
 * @author isv
 */
public interface OpenVPNMapper {

    @Select("SELECT settings.* " +
            "FROM plugin_openvpn_defaults settings " +
            "WHERE customerId=#{customerId}")
    OpenVPNPluginDefaults findSettingsByCustomerId(@Param("customerId") Integer customerId);

    @Insert("INSERT INTO plugin_openvpn_defaults (customerId, removeVpns, removeAll, vpnName, vpnConfig, " +
            "vpnUrl, connect, alwaysOn) VALUES " +
            "(#{customerId}, #{removeVpns}, #{removeAll}, #{vpnName}, #{vpnConfig}, " +
            "#{vpnUrl}, #{connect}, #{alwaysOn})")
    void insertSettings(OpenVPNPluginDefaults settings);

    @Update("UPDATE plugin_openvpn_defaults " +
            "SET " +
            "removeVpns = #{removeVpns}, " +
            "removeAll = #{removeAll}, " +
            "vpnName = #{vpnName}, " +
            "vpnConfig = #{vpnConfig}, " +
            "vpnUrl = #{vpnUrl}, " +
            "connect = #{connect}, " +
            "alwaysOn = #{alwaysOn} " +
            "WHERE id=#{id} AND customerId=#{customerId}")
    void updateSettings(OpenVPNPluginDefaults settings);
}
