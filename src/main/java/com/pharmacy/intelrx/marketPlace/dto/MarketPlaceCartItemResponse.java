package com.pharmacy.intelrx.marketPlace.dto;

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
public class MarketPlaceCartItemResponse {
    private Long id;

    private String intelRxId;

    private String salesStatus;

    private Long inventoryId;

    private Long orderId;

    private Object supplierPharmacy;

    private Object purchaserPharmacy;

    private Object inventoryItem;

    private int quantity;

    private int vat;

    private double amount;

    private LocalDateTime orderDate;

}
