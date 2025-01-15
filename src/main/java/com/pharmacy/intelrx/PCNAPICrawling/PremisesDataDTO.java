package com.pharmacy.intelrx.PCNAPICrawling;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PremisesDataDTO {

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("MiddleName")
    private String middleName;

    @JsonProperty("LastName")
    private String lastName;

    @JsonProperty("PremisesName")
    private String premisesName;

    @JsonProperty("PremisesAddress")
    private String premisesAddress;

    @JsonProperty("DateApproved")
    private LocalDateTime dateApproved;

    @JsonProperty("YearLicenced")
    private String yearLicenced;

    @JsonProperty("PremisesState")
    private String premisesState;

    @JsonProperty("CertificateNo")
    private String certificateNo;

    @JsonProperty("Category")
    private String category;

    @JsonProperty("IsLicencePrinted")
    private boolean isLicencePrinted;

    @JsonProperty("DatePrinted")
    private String datePrinted;

    @JsonProperty("Pharmacist")
    private String pharmacist;

    @JsonProperty("PremisesId")
    private String premisesId;

    @JsonProperty("PharmacistId")
    private String pharmacistId;
}
