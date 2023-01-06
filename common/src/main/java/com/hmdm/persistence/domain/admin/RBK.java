package com.hmdm.persistence.domain.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@ApiModel(description = "This class is required to show the listing of Dashboard Section")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RBK implements Serializable {

    private String id;
    private String rbkLoginId;
    private String rbkName;
    private String vaa;
    private String contact;
    private String status;
    private String statusColour;
    private String lastAccessed;
    private String nonFunctional;
}