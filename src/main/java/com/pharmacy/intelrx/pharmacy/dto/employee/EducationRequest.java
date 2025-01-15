package com.pharmacy.intelrx.pharmacy.dto.employee;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EducationRequest {
    private Long id;
    private Long employeeId;
    private String[] license;

    private Object educationDegree;
    private Object workHistory;

    private List<EducationDegreeRequest> educationDegreeRequests;
    private List<WorkHistoryRequest> workHistoryRequests;
    private String intelRxId;

}
