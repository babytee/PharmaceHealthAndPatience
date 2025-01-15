package com.pharmacy.intelrx.pharmacy.dto.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryRequest {
    private Long id;
    private Long userId;
    private String intelRxId;
    private String inventoryType;
    private String barCodeNumber;
    private String itemName;
    private String brandId;
    private String brandClassId;
    private String brandFormId;
    private Long sizeId;
    private String brandName;
    private String brandClassName;
    private String brandFormName;
    private String sizeName;
    private Integer quantity;
    private double costPrice;
    private double salePrice;
    private double wholeSalePrice;
    private int wholeSaleQuantity;
    private Integer expDay;
    private Integer expMonth;
    private Integer expYear;
    private Boolean poison;
}
