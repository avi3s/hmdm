package com.hmdm.persistence.domain.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@ApiModel(description = "This class is required to show the listing of Dashboard Section")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DashboardDetails implements Serializable {

    private String districtId;
    private String districtName;
    private String installed;
    private String functional;
    private String online;
    private String offline;
    private String nonFunctional;
    private String functionality;
}