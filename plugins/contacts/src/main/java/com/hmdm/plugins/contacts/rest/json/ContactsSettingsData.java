package com.hmdm.plugins.contacts.rest.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Contacts settings")
public class ContactsSettingsData {
    private int configurationId;
    private String vcfUrl;
    private String httpUsername;
    private String httpPassword;
    private String accountType;
    private int syncTimespan;
    private boolean wipeAll;

    public int getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(int configurationId) {
        this.configurationId = configurationId;
    }

    public String getVcfUrl() {
        return vcfUrl;
    }

    public void setVcfUrl(String vcfUrl) {
        this.vcfUrl = vcfUrl;
    }

    public String getHttpUsername() {
        return httpUsername;
    }

    public void setHttpUsername(String httpUsername) {
        this.httpUsername = httpUsername;
    }

    public String getHttpPassword() {
        return httpPassword;
    }

    public void setHttpPassword(String httpPassword) {
        this.httpPassword = httpPassword;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public int getSyncTimespan() {
        return syncTimespan;
    }

    public void setSyncTimespan(int syncTimespan) {
        this.syncTimespan = syncTimespan;
    }

    public boolean isWipeAll() {
        return wipeAll;
    }

    public void setWipeAll(boolean wipeAll) {
        this.wipeAll = wipeAll;
    }
}
