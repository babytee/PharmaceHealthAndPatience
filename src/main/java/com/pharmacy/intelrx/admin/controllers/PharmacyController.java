package com.pharmacy.intelrx.admin.controllers;


import com.pharmacy.intelrx.admin.services.PharmacyService;
import com.pharmacy.intelrx.pharmacy.dto.PharmacyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/admin/pharmacy"})
@RestController("AdminPharmacyController")
public class PharmacyController {

    private final PharmacyService pharmacyService;

    @GetMapping("pharmacy_details/{intelRxId}")
    public ResponseEntity<?>pharmacyDetails(@PathVariable String intelRxId){
        return pharmacyService.pharmacyDetails(intelRxId);
    }

    @GetMapping({"all"})
    public ResponseEntity<?> pharmacies(
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return pharmacyService.pharmacies(filter, pageable);

    }

    @GetMapping({"get_overview_stats"})
    public ResponseEntity<?> pharmacyStats() {
        return pharmacyService.pharmacyStats();
    }

}
