package com.pharmacy.intelrx.pharmacy.dto;

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
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupportTicketResponse {
    private Long id;
    private String emailAddress;
    private String subject;
    private String respondMsg;
    private Object supportType;
    private String ticketNumber;
    private String description;
    private String attachedFile;
    private String ticketStatus;
    private Object resolvedBy;
    private Object pharmacyInfo;
    private LocalDateTime createdAt;
}
