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
public class MedPrescriptionRequest {
    private Long id;

    private String intelRxId;

    private String dosage;

    private Long refillDurationId;

    private Boolean refillAlert;

    private Boolean smsAlert;

    private Long patientId;

    private Long carteItemId;
}
