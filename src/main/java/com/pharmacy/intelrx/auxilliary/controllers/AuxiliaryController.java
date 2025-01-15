package com.pharmacy.intelrx.auxilliary.controllers;

import com.pharmacy.intelrx.utility.Auxiliary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/auxiliary"})
@RestController("adminAuxiliaryController")
public class AuxiliaryController {
    private final Auxiliary auxiliary;

    @GetMapping({"departments"})
    public ResponseEntity<?> allDepartments() {
        return auxiliary.allDepartments();
    }

    @GetMapping({"work_schedules"})
    public ResponseEntity<?> allWorkSchedule() {
        return auxiliary.allWorkSchedule();
    }

    @GetMapping({"document_type"})
    public ResponseEntity<?> allDocumentType() {
        return auxiliary.allDocumentType();
    }

    @GetMapping({"job_title"})
    public ResponseEntity<?> allJobTitle() {
        return auxiliary.allJobTitle();
    }

    @GetMapping({"payment_frequency"})
    public ResponseEntity<?> allPaymentFrequency() {
        return auxiliary.allPaymentFrequency();
    }

    @GetMapping({"salary_type"})
    public ResponseEntity<?> allSalaryType() {
        return auxiliary.allSalaryType();
    }

    @GetMapping({"payment_status"})
    public ResponseEntity<?> allPaymentStatus() {
        return auxiliary.allPaymentStatus();
    }

    @GetMapping({"payment_method"})
    public ResponseEntity<?> allPaymentMethod() {
        return auxiliary.allPaymentMethod();
    }

    @GetMapping({"seniority_level"})
    public ResponseEntity<?> allSeniorityLevel() {
        return auxiliary.allSeniorityLevel();
    }

    @GetMapping({"brands"})
    public ResponseEntity<?> allBrands() {
        return auxiliary.allBrands();
    }

    @GetMapping({"brand_forms"})
    public ResponseEntity<?> brandForm() {
        return auxiliary.allBrandForm();
    }

    @GetMapping({"brand_classes"})
    public ResponseEntity<?> brandClass() {
        return auxiliary.allBrandClass();
    }

    @GetMapping({"sizes"})
    public ResponseEntity<?> sizes() {
        return auxiliary.allSizes();
    }

    @GetMapping({"refill_durations"})
    public ResponseEntity<?> allRefillDurations() {
        return auxiliary.allRefillDurations();
    }

    @GetMapping({"all_kpis_type"})
    public ResponseEntity<?> allKPIs() {
        return auxiliary.allKPIs();
    }

    @GetMapping({"notification_types"})
    public ResponseEntity<?> notificationTypes() {
        return auxiliary.notificationTypes();
    }

    @GetMapping({"support_types"})
    public ResponseEntity<?> supportTypes() {
        return auxiliary.supportTypes();
    }

    @GetMapping({"med_test_types"})
    public ResponseEntity<?> medTestTypes() {
        return auxiliary.medTestTypes();
    }

    @GetMapping({"security_questions"})
    public ResponseEntity<?> securityQuestions() {
        return auxiliary.securityQuestions();
    }

    @GetMapping({"fetch_inventories"})
    public ResponseEntity<?> fetchInventories(
            @RequestParam(name = "inventory_type", required = false) String inventory_type) {
        return auxiliary.fetchInventories(inventory_type);
    }

    @GetMapping({"fetch_med_inventories"})
    public ResponseEntity<?> fetchMedInventories() {
        return auxiliary.fetchMedInventories();
    }

}
