package com.pharmacy.intelrx.pharmacy.dto.employee;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OverviewResponse {
    private Object allPharmacist;
    private Object allCashier;
    private Object allSalesPerson;
    private Object allManager;
    private Object allCleaner;
    private Object allAdministrator;

    private Integer totalEmployees;
    private Integer pharmacists;
    private Integer cashiers;
    private Integer salesPersons;
    private Integer managers;
    private Integer cleaner;
    private Integer administrator;
    private Integer hr;
}
