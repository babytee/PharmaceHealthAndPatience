package com.pharmacy.intelrx.marketPlace.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacy.intelrx.pharmacy.dto.MedPrescriptionRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarketPlaceCartItemRequest {
    private Long id;

    private String intelRxId;

    private Long inventoryId;

    private Long supplierPharmacyId;

    private Long cartItemId;

    private int quantity;

    private int vat;

    private double amount;

}
