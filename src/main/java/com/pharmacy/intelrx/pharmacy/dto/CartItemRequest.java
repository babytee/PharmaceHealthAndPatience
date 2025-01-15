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
public class CartItemRequest {
    private Long id;

    private String intelRxId;

    private Long inventoryId;

    private Long queuedOrderId;

    private Long cartItemId;

    private int quantity;

    private int vat;

    private double amount;

    private Long patientId;

    private MedPrescriptionRequest medPrescriptionRequest;
}
