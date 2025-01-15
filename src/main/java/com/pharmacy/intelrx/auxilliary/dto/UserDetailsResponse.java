package com.pharmacy.intelrx.auxilliary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacy.intelrx.pharmacy.dto.employee.EmployeeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailsResponse {
private Long userId;
private EmployeeResponse employeeResponse;
}
