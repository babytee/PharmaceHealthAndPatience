package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.pharmacy.dto.PatientRequest;
import com.pharmacy.intelrx.pharmacy.dto.TestResultRequest;
import com.pharmacy.intelrx.pharmacy.services.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy"})
@RestController("PharmacyPatientController")
public class PatientController {
    private final PatientService patientService;

    @PostMapping("add_patient")
    public ResponseEntity<?> addPatient(@RequestBody PatientRequest request) {
        return patientService.addPatient(request);
    }

    @PatchMapping("update_patient/{patientId}")
    public ResponseEntity<?> updatePatient(@RequestBody PatientRequest request, @PathVariable Long patientId) {
        return patientService.updatePatient(request, patientId);
    }

    @GetMapping("search_patients")
    public ResponseEntity<?> searchPatients(@RequestParam(name = "keyword", required = false) String keyword) {
        return patientService.searchPatients(keyword);
    }

    @GetMapping("get_all_patients")
    public ResponseEntity<?> getAllPatients(
            @RequestParam(value = "patientSearch", required = false) String patientSearch,
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return patientService.getAllPatients(branchId, patientSearch, pageable);
    }


    @GetMapping("get_patients")
    public ResponseEntity<?> getPatients() {
        return patientService.getPatients();
    }

    @GetMapping("patient_med_purchases/{patientId}")
    public ResponseEntity<?> patientMedPurchaseHistories(@PathVariable Long patientId) {
        return patientService.patientMedPurchaseHistories(patientId);
    }

    @GetMapping("patient_ext_med_purchases")
    public ResponseEntity<?> patientExtMedPurchaseHistories(
            @RequestParam Long patientId,
            @RequestParam String otp) {
        return patientService.patientExtMedPurchaseHistories(patientId, otp);
    }

    @GetMapping("medication_details/{orderId}")
    public ResponseEntity<?> medicationDetails(@PathVariable Long orderId) {
        return patientService.medicationDetails(orderId);
    }

    @GetMapping("send_patient_otp/{patientId}")
    public ResponseEntity<?> generateOtp(@PathVariable Long patientId) {
        return patientService.generateOtp(patientId);
    }
    //getAllTestResult
    @PostMapping("add_test_result")
    public ResponseEntity<?> addTestResult(@RequestBody List<TestResultRequest> requests) {
        return patientService.addTestResult(requests);
    }

    @GetMapping("get_all_test_result/{patientId}")
    public ResponseEntity<?> getAllTestResult(@PathVariable Long patientId) {
        return patientService.getAllTestResult(patientId);
    }

    @GetMapping("get_test_result/{medTestResultId}")
    public ResponseEntity<?> getTestResult(@PathVariable Long medTestResultId) {
        return patientService.getTestResult(medTestResultId);
    }
}
