package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.pharmacy.services.MedPrescriptionRefillAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy"})
@RestController("MedPrescriptionRefillAlertController")
public class MedPrescriptionRefillAlertController {
    private final MedPrescriptionRefillAlertService medPrescriptionRefillAlertService;

    @GetMapping("get_refill_alerts/{patientId}")
    public ResponseEntity<?> getRefillAlerts(@PathVariable Long patientId) {
        return medPrescriptionRefillAlertService.getRefillAlerts(patientId);
    }

    @GetMapping("send_sms_alert")
    public ResponseEntity<?> sendSms(
            @RequestParam(name = "medId", required = true) Long medId,
            @RequestParam(name = "patientId", required = true) Long patientId) {
        return medPrescriptionRefillAlertService.sendSms(medId, patientId);
    }


}
