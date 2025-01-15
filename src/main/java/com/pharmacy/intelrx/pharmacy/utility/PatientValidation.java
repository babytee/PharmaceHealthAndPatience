package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.PatientRequest;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PatientValidation {
    private final Utility utility;

    public ResponseEntity<?> validatePatientRequest(PatientRequest request) {
        if (utility.isNullOrEmpty(request.getName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Name is required"));
        }

        if (utility.isNullOrEmpty(request.getGender())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Gender is required"));
        }

        if (utility.isNullOrEmpty(request.getPhoneNumber())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("PhoneNumber is required"));
        }


        // Additional validation logic if needed...

        return ResponseEntity.ok(StandardResponse.success("Patient request validated successfully"));
    }


}
