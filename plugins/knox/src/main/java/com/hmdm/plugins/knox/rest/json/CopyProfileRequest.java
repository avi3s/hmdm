package com.hmdm.plugins.knox.rest.json;

public class CopyProfileRequest {
    private int configSrc;
    private int configDst;
    private boolean removeExisting;

    public int getConfigSrc() {
        return configSrc;
    }

    public void setConfigSrc(int configSrc) {
        this.configSrc = configSrc;
    }

    public int getConfigDst() {
        return configDst;
    }

    public void setConfigDst(int configDst) {
        this.configDst = configDst;
    }

    public boolean isRemoveExisting() {
        return removeExisting;
    }

    public void setRemoveExisting(boolean removeExisting) {
        this.removeExisting = removeExisting;
    }
}
