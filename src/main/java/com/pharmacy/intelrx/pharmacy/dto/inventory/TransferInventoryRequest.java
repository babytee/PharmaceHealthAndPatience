package com.pharmacy.intelrx.pharmacy.dto.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferInventoryRequest {
    private Long id;
    private String intelRxId;
    private Integer quantity;
    private Long inventoryId;
    private Long transferFromId;
    private Long transferToId;
    private Long receivedById;
}
