package com.pharmacy.intelrx.PCNAPICrawling;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PharmacyPremiseRequest {
    private String premisesId;

    private String pharmacistCategory;

    private String firstName;
    private String middleName;
    private String lastName;
    private String premisesName;
    private String premisesAddress;
    private String premisesState;
    private String certificateNo;
    private String pharmacist;

    private String phoneNumber;
    private String email;
    private String password;
    private String confirmPassword;
}
