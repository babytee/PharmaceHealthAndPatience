package com.pharmacy.intelrx.pharmacy.dto.employee;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacy.intelrx.pharmacy.dto.ContactInfoReqRes;
import com.pharmacy.intelrx.pharmacy.dto.UserRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeRequest {
    private Long id;
    private String employeeType;//CONTRACT or FULL-TIME
    private String employeeIntelRxId;

    private UserRequest userRequest;
    private ContactInfoReqRes contactInfoReqRes;
    private JobInformationRequest jobInformationRequest;
    private CompensationDetailRequest compensationDetailRequest;
    private EducationRequest educationRequest;
    private LegalRequest legalRequest;
    private BenefitRequest benefitRequest;
    private ExtraInformationRequest extraInformationRequest;
    private List<EmployeeDocumentRequest> employeeDocumentRequest;
}
