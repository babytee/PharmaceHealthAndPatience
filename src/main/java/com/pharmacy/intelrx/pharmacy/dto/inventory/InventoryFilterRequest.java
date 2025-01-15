package com.pharmacy.intelrx.pharmacy.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryFilterRequest {

    private String inventoryType;
    private String intelRxId;
    private Boolean poison;
    private Long barCodeNumber;
    private Long itemName;
    private Long brandId;
    private Long brandClassId;
    private Long brandFormId;
    private Long sizeId;
    private Long branchId;
    private String searchText;
}
