package com.pharmacy.intelrx.pharmacy.dto;

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
public class ConcludeEmployeeRequest {
    private Long id;
    private Long employeeId;
    private String concludeType;//TERMINATED,DELETED,SUSPENDED,END CONTRACT
    private String reasons;
    private String password;
}
