package com.hmdm.plugins.openvpn.rest.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmdm.plugins.openvpn.persistence.domain.OpenVPNPluginDefaults;
import io.swagger.annotations.ApiModel;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "OpenVPN run app command")
public class OpenVPNRunAppData {
    private String scope;
    private int configurationId;
    private String deviceNumber;

    public OpenVPNRunAppData() {}

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public int getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(int configurationId) {
        this.configurationId = configurationId;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }
}
