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
public class DiscountRequest {
    private Long id;
    private String intelRxId;
    private boolean discountSwitch;
    private double medicationPercentage;
    private double groceryPercentage;
}
