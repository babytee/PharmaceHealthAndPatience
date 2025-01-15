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
public class SalaryPaymentRequest {
    private Long id;

    private Long employeeId;

    private String payPeriod;

    private double amount;

    private String status;//Paid,OverDue

    private String paymentReceipt;
}
