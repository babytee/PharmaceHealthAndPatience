package com.pharmacy.intelrx.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PharmacyRequest {
    private Long id;
    private String pharmacyName;
    private String premiseNumber;
    private String premisesId;
    private String phoneNumber;
    private String[] otherNumber;
    private String pharmacyOwner;
    private String logo;
    private String intelRxId;
    private String website;
    private String twitter;
    private String linkedIn;
    private String regBy;
    private String subscriptionStatus;
    private String pharmacistCategory;

    private ContactInfoReqRes contactInfoReqRes;
    private LocalDateTime createdAt;
}
