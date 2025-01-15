package com.pharmacy.intelrx.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientRequest {
    private Long id;
    private String name;
    private String gender;
    private String phoneNumber;
    private String intelRxId;
    private String address;
    private String dob;
    private String email;
    private LocalDateTime dateAdded;
}
