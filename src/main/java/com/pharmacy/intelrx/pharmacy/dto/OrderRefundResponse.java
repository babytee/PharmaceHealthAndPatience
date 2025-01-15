package com.pharmacy.intelrx.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderRefundResponse {
    @JsonIgnore
    private Long id;

    @JsonIgnore
    private String intelRxId;

    @JsonIgnore
    private String orderRef;

    @JsonIgnore
    private String reasonForRefund;

    @JsonIgnore
    private String optionalReason;

    private Double refundAmount;
    
    private Object refundMethod;

    private Object refundedBy;

    @JsonIgnore
    private Object pharmacyBranch;

    private LocalDateTime refundDate;
}
