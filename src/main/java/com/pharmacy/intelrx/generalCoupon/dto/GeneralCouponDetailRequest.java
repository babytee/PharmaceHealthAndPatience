package com.pharmacy.intelrx.generalCoupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralCouponDetailRequest {
    private Long id;
    private String couponCode;
    private LocalDate dateUsed;
    private LocalTime timeUsed;
    private double amountPerPerson;

    private Object patient;
    private String pharmacy;
    private String location;
}
