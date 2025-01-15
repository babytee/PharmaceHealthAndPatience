package com.pharmacy.intelrx.generalCoupon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralCouponRequest {
    private Long id;
    private String couponCode;
    private String couponTitle;
    private String couponEventType;
    private String couponDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private double couponValue;

    private String sharingCapacity; //automatic or manual
    private String couponVicinity; //State or Pharmacy
    private String[] stateVicinity;

    @Valid
    @Min(0)
    private Long[] pharmacyVicinityId;

    @Valid
    @Min(0)
    private Long[] pharmacyBranchVicinityId;

    private double amountPerPerson;
    private int countPerPerson;
    //this works for sharingCapacity when it is automatic
    //private String coverage;// All States or Some Specific State

    private String couponStatus; //active,expired,disabled
}
