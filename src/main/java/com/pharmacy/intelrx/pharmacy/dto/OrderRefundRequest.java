package com.pharmacy.intelrx.pharmacy.dto;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderRefundRequest {
    private Long id;

    private String intelRxId;

    private Long orderId;

    private Long cartItemId;

    private String reasonForRefund;

    private String optionalReason;

    private Double refundAmount;

    private Long paymentMethodId;

    private LocalDateTime refundDate;
}
