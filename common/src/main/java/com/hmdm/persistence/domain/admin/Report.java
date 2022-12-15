package com.hmdm.persistence.domain.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@ApiModel(description = "This class is required to show the listing of Dashboard Section")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Report implements Serializable {

    private String id;
    private String rbkId;
    private String rbkName;
    private String vaa;
    private String contact;
    private String mandalName;
    private String districtName;
    private String secretariatCode;
    private String status;
    private String networkType;
    private String lastContact;
}