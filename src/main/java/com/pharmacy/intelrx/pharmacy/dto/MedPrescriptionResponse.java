package com.pharmacy.intelrx.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacy.intelrx.pharmacy.models.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class MedPrescriptionResponse {
    private Long id;

    private String intelRxId;

    private String dosage;

    private Object refillDuration;

    private Boolean refillAlert;

    private Boolean smsAlert;

    private Object patient;

    private Object branch;

    private Object cartItem;

    private LocalDateTime refillReminderDate;

    private String alertMessage; // New field for the alert message

}
