package com.hmdm.persistence.domain.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@ApiModel(description = "This class is required to show the listing of Dashboard Section")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RBKPage implements Serializable {

    private List<RBK> rbks;
    private String totalKioskCount;
    private String functionalCount;
    private String onlineCount;
    private String offlineCount;
    private String nonfunctionalCount;
}