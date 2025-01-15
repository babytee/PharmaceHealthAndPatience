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
public class OutOfStockResponse {
    private Long inventoryId;
    private String stockLeft;
    private String description;
    private Object inventoryDetails;
}
