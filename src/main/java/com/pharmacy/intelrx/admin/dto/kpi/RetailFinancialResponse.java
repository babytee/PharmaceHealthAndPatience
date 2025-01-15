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
public class RetailFinancialResponse {
    private double OPEXAmount;
    private double quickRationPercentage;
    private double currentRationPercentage;
    private double accountsPayableTurnoverAmount;
    private String daysOfInventoryOutstanding;
    private String daysPayableOutstanding;
    private String cashConversionCycle;
    private double netProfitMarginPercentage;
}
