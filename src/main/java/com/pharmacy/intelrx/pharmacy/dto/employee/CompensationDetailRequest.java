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
public class CompensationDetailRequest {
    private Long id;
    private Long employeeId;
    private Long salaryTypeId;
    private Long paymentFrequencyId;

    private String salaryType;
    private String paymentFrequency;
    private String salaryStatus;

    private String salary;
    private String bankName;
    private String accountNumber;
    private String accountName;

    private String intelRxId;
}
