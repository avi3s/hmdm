package com.hmdm.persistence.domain.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@ApiModel(description = "This class is required to show the listing of Dashboard Section")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RedList implements Serializable {

    private String id;
    private String rbkLoginId;
    private String rbkName;
    private String underMaintenance;
    private String contact;
    private String mandalName;
    private String districtName;
    private String status;
}