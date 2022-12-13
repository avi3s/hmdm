package com.hmdm.plugins.devicereset.rest.json;

public class PasswordResetRequest {
    private int deviceId;
    private String password;

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
