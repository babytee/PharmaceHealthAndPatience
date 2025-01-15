package com.pharmacy.intelrx.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpenditureRequest {
    private Long id;
    private String intelRxId;
    private String expenseName;
    private double amountSpent;
    private int expDay;
    private int expMonth;
    private int expYear;
    private String expenditureType;
    private Object addedBy;
    private Object approvedBy;

    @JsonIgnore
    private Long approvedById;//employeeId

    private boolean approved;

    private LocalDateTime createdAt;
}
