package com.pharmacy.intelrx.pharmacy.controllers;


import com.pharmacy.intelrx.pharmacy.dto.PharmacyRequest;
import com.pharmacy.intelrx.pharmacy.services.PharmacyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy"})
@RestController("PharmacyController")
public class PharmacyController {

    private final PharmacyService pharmacyService;

    @PostMapping({"verify_pharmacy"})
    public ResponseEntity<?> verifyPharmacy(@RequestBody PharmacyRequest request) {
        return pharmacyService.verifyPharmacy(request);
    }

    @GetMapping({"pharmacy_details"})
    public ResponseEntity<?> pharmacyDetails() {
        return pharmacyService.pharmacyDetails();
    }
}
