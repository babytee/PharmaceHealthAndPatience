package com.pharmacy.intelrx.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PharmacyStatResponse {
    private int totalPharmacy;
    private int lessThanLastMonth;
    private int selfRegisterPercent;
    private int salesRepRegisterPercent;
    private int totalStateCovered;
    private String topLocation;
    private int pharmacySubscribed;
}
