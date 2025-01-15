package com.pharmacy.intelrx.admin.dto.kpi;

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
public class KPIRecommendationRequest {
    private Long id;

    private String intelRxId;

    private Long kpiTypeId;

    private Object kpiType;

    private String recommendationMsg;

    private String docs;

    private LocalDateTime createdAt;
}
