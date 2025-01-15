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
public class MarketPlaceOrderResponse {
    private Long id;

    private String intelRxId;

    private String orderRef;

    private double totalAmount;

    private Object billingInfo;

    private String salesStatus;//Placed,Confirmed,Processed,Pickup,Delivery,Completed

    private Object orderItems;

    private int totalQuantity;

    private String deliveryMethod;//Pickup,Delivery

    private int vat;

    private double amount;

    private int medicationItems;

    private int groceryItems;

    private LocalDateTime deliveryDate;

    private LocalDateTime orderedDate;

}
