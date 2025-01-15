package com.pharmacy.intelrx.admin.dto.kpi;

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
public class SalesDataEvaluationResponse {
    private double yoySalesAmount;
    private double averageTransactionAmount;
    private double costOfGoodsSoldAmount;
    private double GMROI;
    private double shrinkageAmount;
    private double sellThroughPercentage;
}
