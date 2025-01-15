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
public class NotificationEntityRequest {
    private Long id;
    private String intelRxId;
    private Long notificationTypeId;
    private Long employeeId;
    private Long branchId;
    private String notificationTitle;
    private String notificationMsg;
    private boolean notificationStatus;
}
