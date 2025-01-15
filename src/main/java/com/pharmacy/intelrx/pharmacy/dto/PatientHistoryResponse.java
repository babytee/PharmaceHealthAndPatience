package com.pharmacy.intelrx.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class PatientHistoryResponse {
    @JsonIgnore
    private Long orderId;
    private Object patientDetails;
    private String pharmacyVisited;
    private String location;
    private int pharmacyVisits;
    private double lifeTimePurchase;
    private LocalDateTime firstPurchaseDate;
    private LocalDateTime dateAdded;
}
