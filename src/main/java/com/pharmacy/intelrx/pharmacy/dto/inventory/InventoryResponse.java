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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryResponse {
    private Long id;
    private Long userId;
    private String inventoryType;
    private String intelRxId;
    private String barCodeNumber;
    private String itemName;
    private String genericName;

    private Object brand;
    private Object brandClass;
    private Object brandForm;
    private Object medicationSize;


    private Double wholeSalePrice;
    private Integer wholeSaleQuantity;

    private Integer quantity;
    private Double costPrice;
    private Double salePrice;
    private Integer expDay;
    private Integer expMonth;
    private Integer expYear;
    private Boolean poison;
    private String category;


    private Object supplier;

}
