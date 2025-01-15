package com.pharmacy.intelrx.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PharmacyBranchResponse {
    private Long id;
    private String name;
    private String pharmacyIntelRxId;
    private String bgColor;
    private Object ManagerInfo;
    private Object pharmacyInfo;
    private Object contactInfo;
}
