package com.pharmacy.intelrx.admin.dto;

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
public class PharmacyBranchStatResponse {
    private int employees;
    private int Medication;
    private int Grocery;
    private String Manager;
}
