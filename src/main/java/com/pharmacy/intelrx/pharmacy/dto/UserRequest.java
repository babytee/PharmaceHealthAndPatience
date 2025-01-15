package com.pharmacy.intelrx.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacy.intelrx.auxilliary.models.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRequest {
    private Long id;
    private Long employeeId;
    private UserType userType;//EMPLOYEE or OWNER or BRAND
    private String companyName;
    private String firstName;
    private String lastName;
    private Integer birthMonth;
    private Integer yearOfBirth;
    private Integer dayOfBirth;
    private String userStatus;//Online,Offline,Suspended
    private String gender;
    private String email;
    private String phoneNumber;
    private String profilePic;
    private String intelRxId;
    private String pharmacistCategory;
    private String password;
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
    private Boolean twoFactorAuth;
}
