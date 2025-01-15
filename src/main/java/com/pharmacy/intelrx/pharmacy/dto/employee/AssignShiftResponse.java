package com.pharmacy.intelrx.pharmacy.dto.employee;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.JobTitle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignShiftResponse {

    private Long id;
    private String intelRxId;
    private EmployeeResponse assignedMember;//employee id
    private JobTitle jobTitle;
    private String startTime;
    private String endTime;
    private String shiftHours;
    private LocalDate shiftDate;
    private String status;//Paid,Overdue, Due duration left
    private LocalDateTime createdAt;
    private String assignedColor;
    private Object employeeInfo;

}
