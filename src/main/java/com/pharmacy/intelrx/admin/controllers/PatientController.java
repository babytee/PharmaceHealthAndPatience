package com.pharmacy.intelrx.admin.controllers;

import com.pharmacy.intelrx.admin.services.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/admin/patient"})
@RestController("AdminPatientController")
public class PatientController {
    private final PatientService patientService;

    @GetMapping("pharmacy_patients")
    public ResponseEntity<?> getPharmacyPatients(
            @RequestParam(value = "patientSearch", required = false) String patientSearch,
            @RequestParam(value = "intelRxId", required = true) String intelRxId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return patientService.getPharmacyPatients(patientSearch,intelRxId, pageable);
    }

    @GetMapping("pharmacy_branch_patients")
    public ResponseEntity<?> getPharmacyBranchPatients(
            @RequestParam(value = "patientSearch", required = false) String patientSearch,
            @RequestParam(value = "intelRxId", required = true) String intelRxId,
            @RequestParam(value = "branchId", required = true) Long branchId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        //return ResponseEntity.ok(branchId);

        return patientService.getPharmacyBranchPatients(patientSearch,intelRxId,branchId, pageable);
    }


    @GetMapping("get_all_patients")
    public ResponseEntity<?> getAllPatients(
            @RequestParam(value = "patientSearch", required = false) String patientSearch,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return patientService.getAllPatients(patientSearch, pageable);
    }


    @GetMapping("get_patients")
    public ResponseEntity<?> getPatients(){
        return patientService.getPatients();
    }

    @GetMapping("get_patient_details/{patientId}/{intelRxId}")
    public ResponseEntity<?> getPatientDetails(@PathVariable Long patientId,@PathVariable String intelRxId) {
        return patientService.getPatientDetails(patientId,intelRxId);
    }

    @GetMapping("patient_med_purchases/{patientId}")
    public ResponseEntity<?> patientMedPurchaseHistories(@PathVariable Long patientId) {
       return patientService.patientMedPurchaseHistories(patientId);
    }



    @GetMapping("medication_details/{orderId}")
    public ResponseEntity<?>medicationDetails(@PathVariable Long orderId){
        return patientService.medicationDetails(orderId);
    }
}
