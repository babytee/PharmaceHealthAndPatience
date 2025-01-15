package com.pharmacy.intelrx.pharmacy.dto.employee;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignShiftRequest {

    private Long id;
    private String intelRxId;

    private Long[] assignedMemberId;//employee id

    private Long employeeId;

    private Long branchId;//branchId

    private Long jobTitleId;//jobTitleId

    private String startDate;

    private String endDate;

    private String startTime;

    private String endTime;

    private String shiftTime;

    private String status;//Paid,Overdue, Due duration left

    private String AssignedColor;
}
