package com.pharmacy.intelrx.pharmacy.dto;

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
public class MonthlyExpenditureRevenueAnalyticResponse {
    private Object monthlyRevenue;
    private Object getMonthlyExpenditurePercentage;
    private Object getMonthlyExpenditureBreakDown;
}
