package com.pharmacy.intelrx.pharmacy.dto.employee;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeFilterRequest {
    private Long UserId;
    private String workerStatus;//Online,Offline,Suspended
    private String employeeType;//CONTRACT or FULL-TIME
    private String salaryStatus;//Paid or Overdue
    private Long salaryTypeId;
    private Long jobTitleId;
    private Long branchId;
    private Integer birthMonth;
    private String intelRxId;
    private String searchText;
}
