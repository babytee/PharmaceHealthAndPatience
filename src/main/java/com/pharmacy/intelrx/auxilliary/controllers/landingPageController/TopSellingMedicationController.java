package com.pharmacy.intelrx.auxilliary.controllers.landingPageController;

import com.pharmacy.intelrx.auxilliary.LandingPages.TopSellingMedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("TopSellingMedicationController")
@RequestMapping({"/api/v1/top_selling"})
public class TopSellingMedicationController {
    private final TopSellingMedicationService medicationService;

    @GetMapping("medications")
    public ResponseEntity<?> topSellingProducts
            (
                    @RequestParam(name = "timePeriod",required = false,defaultValue = "1 Month") String timePeriod,
                    @RequestParam(name = "brandId",required = false) Long brandId,
                    @RequestParam(name = "medicationId", required = false) Long medicationId
            ) {
        return medicationService.topSellingProducts(timePeriod, brandId, medicationId);
    }
}
