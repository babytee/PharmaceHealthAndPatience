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
public class OutOfStockNotificationRequest {
    private Long id;
    private String intelRxId;
    private Boolean checkNotification;
    private int medication;
    private int grocery;
}
