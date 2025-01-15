package com.pharmacy.intelrx.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class PharmacyBranchesResponse {
    private Object headQuarters;
    private Object branches;
    private Object employees;
    private Object patients;
    private Integer totalBranches;
    private Integer totalEmployees;
    private Integer totalPatients;
    private Integer totalMedications;
    private Integer totalGrocery;
}
