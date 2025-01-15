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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationEntityResponse {
    private Long id;
    private String intelRxId;
    private Object branchDetails;
    private Object notificationType;
    private Object employeeDetails;
    private String notificationTitle;
    private String notificationMsg;
    private boolean notificationStatus;
    private LocalDateTime notificationDate;
}
