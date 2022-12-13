package com.hmdm.plugins.openvpn.rest.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmdm.plugins.openvpn.persistence.domain.OpenVPNPluginDefaults;
import io.swagger.annotations.ApiModel;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "OpenVPN settings")
public class OpenVPNSettingsData {
    private int configurationId;        // 0 for default settings
    private String removeVpns;
    private Boolean removeAll;
    private String vpnName;
    private String vpnConfig;
    private String vpnUrl;
    private Boolean connect;
    private Boolean alwaysOn;

    public OpenVPNSettingsData() {}

    public OpenVPNSettingsData(OpenVPNPluginDefaults defaults) {
        removeVpns = defaults.getRemoveVpns();
        removeAll = defaults.isRemoveAll();
        vpnName = defaults.getVpnName();
        vpnConfig = defaults.getVpnConfig();
        vpnUrl = defaults.getVpnUrl();
        connect = defaults.isConnect();
        alwaysOn = defaults.isAlwaysOn();
    }

    public int getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(int configurationId) {
        this.configurationId = configurationId;
    }

    public String getRemoveVpns() {
        return removeVpns;
    }

    public void setRemoveVpns(String removeVpns) {
        this.removeVpns = removeVpns;
    }

    public Boolean getRemoveAll() {
        return removeAll;
    }

    public void setRemoveAll(Boolean removeAll) {
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

    public Boolean getConnect() {
        return connect;
    }

    public void setConnect(Boolean connect) {
        this.connect = connect;
    }

    public Boolean getAlwaysOn() {
        return alwaysOn;
    }

    public void setAlwaysOn(Boolean alwaysOn) {
        this.alwaysOn = alwaysOn;
    }
}
