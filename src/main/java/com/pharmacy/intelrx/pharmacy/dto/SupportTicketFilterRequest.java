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
public class SupportTicketFilterRequest {
    private Long id;
    private String intelRxId;
    private String keyword;
    private String password;
    private String state;
    private Long supportTypeId;
    private String ticketStatus;
    private String respondMsg;
}
