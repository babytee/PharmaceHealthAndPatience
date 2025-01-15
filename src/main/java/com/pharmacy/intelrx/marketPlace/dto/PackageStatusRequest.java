package com.pharmacy.intelrx.marketPlace.dto;

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
public class PackageStatusRequest {

    private Long id;
    private String intelRxId;
    private Long itemId;
    private String deliveryStatus;
    private Object itemDetails;
    private LocalDateTime createdAt;

}
