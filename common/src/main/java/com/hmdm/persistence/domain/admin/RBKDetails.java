package com.hmdm.persistence.domain.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@ApiModel(description = "This class is required to show the listing of Dashboard Section")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RBKDetails implements Serializable {

    private String id;
    private String rbkLoginId;
    private String rbkName;
    private String underMaintenance;
    private String contact;
    private String mandalName;
    private String districtName;
    private String designation;
    private String emailAddress;
    private String otaURL;
    private String phone;
    private String uniqueValue;
    private String vaaName;
    private String vaaAddress;
    private String secretariatCode;
    private String secretariatAddress;
    private String kioskStatus;
    private String networkType;
    private String assigned;
    private String adaSubDivision;
    private String created;
    private String lastContact;
    private String active;
    private String cfmsID;
    private String latitude;
    private String longitude;
    private String uniqueID;
    private String ethernetMACId;
    private String wifiMACId;
    private String deviceFingerpint;
    private String nameOfMAO;
    private String maoContact;
    private String speedOfExistingNetwork;
    private String apFibernetAvailableOrNot;
    private String imeiNo;
    private String defaultLauncher;
}