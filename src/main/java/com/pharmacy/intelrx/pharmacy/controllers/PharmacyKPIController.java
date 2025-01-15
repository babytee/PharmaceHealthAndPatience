package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.pharmacy.services.PharmacyKPIServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy/kpi"})
@RestController("PharmacyKPIController")
public class PharmacyKPIController {
    private final PharmacyKPIServices pharmacyKpiServices;

    @GetMapping("sales_data_evaluation")
    public ResponseEntity<?> salesDataEvaluation(
            @RequestParam(name = "duration") String duration
    ) {
        return pharmacyKpiServices.salesDataEvaluation(duration);
    }

    @GetMapping("merchandising")
    public ResponseEntity<?> merchandising(
            @RequestParam(name = "duration") String duration
    ) {
        return pharmacyKpiServices.merchandising(duration);
    }

    @GetMapping("customer_evaluation")
    public ResponseEntity<?> customerEvaluation(
            @RequestParam(name = "duration") String duration
    ) {
        return pharmacyKpiServices.customerEvaluation(duration);
    }

    @GetMapping("retail_financial")
    public ResponseEntity<?> retailFinancial(
            @RequestParam(name = "duration") String duration
    ) {
        return pharmacyKpiServices.retailFinancial(duration);
    }


    @GetMapping("get_recommendation/{id}")
    public ResponseEntity<?>getRecommendation(
            @PathVariable Long id
    ){
        return pharmacyKpiServices.getRecommendation(id);
    }

    @GetMapping("get_recommendations/{kpiTypeId}")
    public ResponseEntity<?>getAllRecommendation(
            @PathVariable Long kpiTypeId
    ){
        return pharmacyKpiServices.getAllRecommendation(kpiTypeId);
    }

}
