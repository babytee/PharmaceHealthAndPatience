package com.pharmacy.intelrx.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PharmacyBranchRequest {
    private Long id;
    private String name;
    private String pharmacyIntelRxId;
    private Long employeeId;
    private String bgColor;
    private ContactInfoReqRes contactInfoReqRes;
    //private BranchEmployeeDTO branchEmployeeDTO;
}
