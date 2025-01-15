package com.pharmacy.intelrx.pharmacy.dto;

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
public class CartItemResponse {
    private Long id;

    private Long orderId;

    private String intelRxId;

    private Object inventoryItem;

    private int quantity;

    private int vat;

    private double amount;

    private Object patient;

    private Object medPrescription;

    private Object branch;

    private Object itemRefund;
}
