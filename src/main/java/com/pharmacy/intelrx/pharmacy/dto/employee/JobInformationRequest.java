package com.pharmacy.intelrx.pharmacy.dto.employee;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class JobInformationRequest {
    private Long id;
    private Long employeeId;
    private Long seniorityLevelId;
    private Long jobTitleId;
    private Long departmentId;
    private Long workScheduleId;

    private String seniorityLevel;
    private String jobTitle;
    private String department;
    private String workSchedule;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String jobScope;


    private String intelRxId;
}
