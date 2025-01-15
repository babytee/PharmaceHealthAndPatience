package com.pharmacy.intelrx.generalCoupon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacy.intelrx.generalCoupon.models.CouponType;
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
public class GeneralCouponResponse {
    private Long id;
    private String couponCode;
    private String couponTitle;
    private CouponType couponType;
    private String couponEventType;
    private String couponDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private double couponValue;

    private String sharingCapacity; //automatic or manual
    private String couponVicinity; //State or Pharmacy
    private String stateVicinity;
    private String pharmacyVicinity;

    private double amountPerPerson;
    private int countPerPerson;
    private int couponUsed;

    //this works for sharingCapacity when it is automatic
    private String coverage;// All States or Some Specific State

    private String couponStatus; //active,expired,disabled
}
