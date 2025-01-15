package com.pharmacy.intelrx.pharmacy.dto.employee;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacy.intelrx.pharmacy.dto.ContactInfoReqRes;
import com.pharmacy.intelrx.pharmacy.dto.UserRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeResponse {
    private Long id;
    private String employeeType;//CONTRACT or FULL-TIME
    private String workerStatus;//Online,Offline,Suspended
    private String employeeIntelRxId;
    private Boolean status;
    private UserRequest personalInformation;
    private ContactInfoReqRes contactInformation;
    private JobInformationRequest jobInformation;
    private CompensationDetailRequest compensationDetails;
    private EducationRequest educationQualification;
    private LegalRequest legal;
    private Object benefit;
    private Object extraInformation;
    private Object employeeDocuments;
    private Object concludedEmployeeStatus;
    private Object AssignShiftResponse;
    private Object branches;
}
