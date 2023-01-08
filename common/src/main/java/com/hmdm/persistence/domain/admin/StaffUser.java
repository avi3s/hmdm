package com.hmdm.persistence.domain.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@ApiModel(description = "This class is required for Staff Users")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StaffUser implements Serializable {

    private String staffId;
    private String email;
    private String firstname;
    private String lastname;
    private String phonenumber;
    private String password;
    private String admin;
    private String authToken;
    private String lastLogin;
}