package com.pharmacy.intelrx.pharmacy.dto.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacy.intelrx.auxilliary.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferInventoryResponse {
    private Long id;
    private String intelRxId;
    private Integer quantity;
    private Object inventory;
    private Object transferFrom;
    private Object transferOfficer;
    private Object transferTo;
    private Object receivedBy;
    private LocalDate dateSent;
    private LocalDate receivedTime;
    private String status;//Pending, Cancelled,Received
}
