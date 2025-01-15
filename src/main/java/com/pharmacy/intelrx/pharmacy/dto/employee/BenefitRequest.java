package com.pharmacy.intelrx.pharmacy.dto.employee;

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
public class BenefitRequest {
    private Long id;
    private String[] benefits;
    private String beneficiary;
    private String intelRxId;
}
