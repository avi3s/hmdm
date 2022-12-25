package com.hmdm.persistence.domain.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@ApiModel(description = "This class is required to pass any inputs for the Reports")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Input implements Serializable {

    private String startDate;
    private String endDate;
    private String districtId;
    private String districtName;
    private String mandalId;
    private String mandalName;
    private String kioskStatus;
}
