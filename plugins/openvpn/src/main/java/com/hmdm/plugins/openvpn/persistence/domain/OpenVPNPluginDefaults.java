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

package com.hmdm.plugins.openvpn.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmdm.persistence.domain.CustomerData;
import com.hmdm.plugins.openvpn.rest.json.OpenVPNSettingsData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>A domain object representing a single collection of plugin settings per customer account.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "A collection of 'OpenVPN' plugin default settings")
public class OpenVPNPluginDefaults implements CustomerData, Serializable {

    private static final long serialVersionUID = 6862393507577533571L;

    @ApiModelProperty("An ID of the record")
    private Integer id;

    // An ID of a customer account which these settings correspond to
    @ApiModelProperty(hidden = true)
    private int customerId;

    @ApiModelProperty("List of VPN record names to remove from devices")
    private String removeVpns;

    @ApiModelProperty("A flag indicating if all VPN records should be removed from devices")
    private boolean removeAll;

    @ApiModelProperty("VPN name")
    private String vpnName;

    @ApiModelProperty("Path of the VPN configuration file on the device")
    private String vpnConfig;

    @ApiModelProperty("Url of the VPN configuration file (.ovpn)")
    private String vpnUrl;

    @ApiModelProperty("A flag indicating if VPN should be connected after setting up")
    private boolean connect;

    @ApiModelProperty("A flag indicating if VPN should be set as always-on")
    private boolean alwaysOn;

    /**
     * <p>Constructs new <code>OpenVPNPluginDefaults</code> instance. This implementation does nothing.</p>
     */
    public OpenVPNPluginDefaults() {
    }

    public void load(OpenVPNSettingsData data) {
        removeVpns = data.getRemoveVpns();
        removeAll = data.getRemoveAll() != null ? data.getRemoveAll() : false;
        vpnName = data.getVpnName();
        vpnConfig = data.getVpnConfig();
        vpnUrl = data.getVpnUrl();
        connect = data.getConnect() != null ? data.getConnect() : false;
        alwaysOn = data.getAlwaysOn() != null ? data.getAlwaysOn() : false;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int getCustomerId() {
        return customerId;
    }

    @Override
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getRemoveVpns() {
        return removeVpns;
    }

    public void setRemoveVpns(String removeVpns) {
        this.removeVpns = removeVpns;
    }

    public boolean isRemoveAll() {
        return removeAll;
    }

    public void setRemoveAll(boolean removeAll) {
        this.removeAll = removeAll;
    }

    public String getVpnName() {
        return vpnName;
    }

    public void setVpnName(String vpnName) {
        this.vpnName = vpnName;
    }

    public String getVpnConfig() {
        return vpnConfig;
    }

    public void setVpnConfig(String vpnConfig) {
        this.vpnConfig = vpnConfig;
    }

    public String getVpnUrl() {
        return vpnUrl;
    }

    public void setVpnUrl(String vpnUrl) {
        this.vpnUrl = vpnUrl;
    }

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }

    public boolean isAlwaysOn() {
        return alwaysOn;
    }

    public void setAlwaysOn(boolean alwaysOn) {
        this.alwaysOn = alwaysOn;
    }

    @Override
    public String toString() {
        return "OpenVPNPluginDefaults{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", removeAll=" + removeAll +
                ", removeVpns='" + removeVpns + '\'' +
                ", vpnName='" + vpnName + '\'' +
                ", vpnConfig='" + vpnConfig + '\'' +
                ", vpnUrl='" + vpnUrl + '\'' +
                ", connect=" + connect +
                ", alwaysOn=" + alwaysOn +
                '}';
    }
}
