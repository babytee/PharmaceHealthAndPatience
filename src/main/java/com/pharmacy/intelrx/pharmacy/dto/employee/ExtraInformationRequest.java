package com.pharmacy.intelrx.pharmacy.dto.employee;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtraInformationRequest {
    private Long id;
    private String preferredNickname;
    private String emergencyContactName;
    private String emergencyContactNumber;
    private String relationshipWithEmergency;
    private String refereeName;
    private String refereeNumber;
    private String relationshipWithReferee;
    private String disabilityStatus;
    private String[] language;
    private String intelRxId;
}
