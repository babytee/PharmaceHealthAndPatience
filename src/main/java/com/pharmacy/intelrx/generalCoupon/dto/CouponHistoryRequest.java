package com.pharmacy.intelrx.generalCoupon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacy.intelrx.pharmacy.models.Order;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponHistoryRequest {
    private Long id;
    private Long orderId;
    private String intelRxId;
    private String trxRef;
    private String description;
    private Double amount;
    private String trxStatus;
    private String couponCode;

    private Order order;

    @NotNull(message = "trxType cannot be null and it must be debit or credit")
    @Pattern(regexp = "debit|credit", message = "trxType must be either 'debit' or 'credit'")
    private String trxType;

    @NotNull(message = "trxCouponType cannot be null and it must be BRAND, GENERAL or IN_HOUSE")
    @Pattern(regexp = "BRAND|GENERAL|IN_HOUSE", message = "trxCouponType must be 'BRAND', 'GENERAL', or 'IN_HOUSE'")
    private String trxCouponType;
    private LocalDateTime createdAt;
}
