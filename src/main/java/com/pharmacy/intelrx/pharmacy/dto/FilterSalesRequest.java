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
public class FilterSalesRequest {
    private Long branchId;
    private String startDate;
    private String endDate;
    private Long salesPersonId;
    private Long cashierId;
    private String salesStatus;
    private String intelRxId;
    private String dateFilter;
}
