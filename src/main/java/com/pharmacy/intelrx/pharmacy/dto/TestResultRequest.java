package com.pharmacy.intelrx.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestResultRequest {
    private Long id;
    private String intelRxId;
    @NotNull(message = "testTypeId is required")
    private Long testTypeId;

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotBlank(message = "testResult is required")
    private String testResult;

    @NotBlank(message = "testNotes is required")
    private String testNotes;
}
