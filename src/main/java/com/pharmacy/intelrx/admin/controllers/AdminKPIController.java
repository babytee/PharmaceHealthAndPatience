package com.pharmacy.intelrx.admin.controllers;

import com.pharmacy.intelrx.admin.dto.kpi.KPIRecommendationDeleteRequest;
import com.pharmacy.intelrx.admin.dto.kpi.KPIRecommendationRequest;
import com.pharmacy.intelrx.admin.services.AdminKPIServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/admin/kpi"})
@RestController("AdminKPIController")
public class AdminKPIController {
    private final AdminKPIServices adminKpiServices;

    @GetMapping("sales_data_evaluation")
    public ResponseEntity<?> salesDataEvaluation(
            @RequestParam(name = "intelRxId") String intelRxId,
            @RequestParam(name = "duration") String duration
    ) {
        return adminKpiServices.salesDataEvaluation(intelRxId, duration);
    }

    @GetMapping("merchandising")
    public ResponseEntity<?> merchandising(
            @RequestParam(name = "intelRxId") String intelRxId,
            @RequestParam(name = "duration") String duration
    ) {
        return adminKpiServices.merchandising(intelRxId, duration);
    }

    @GetMapping("customer_evaluation")
    public ResponseEntity<?> customerEvaluation(
            @RequestParam(name = "intelRxId") String intelRxId,
            @RequestParam(name = "duration") String duration
    ) {
        return adminKpiServices.customerEvaluation(intelRxId, duration);
    }

    @GetMapping("retail_financial")
    public ResponseEntity<?> retailFinancial(
            @RequestParam(name = "intelRxId") String intelRxId,
            @RequestParam(name = "duration") String duration
    ) {
        return adminKpiServices.retailFinancial(intelRxId, duration);
    }

    @PostMapping("add_recommendation")
    public ResponseEntity<?>addRecommendation(@RequestBody KPIRecommendationRequest request) throws IOException {
        return adminKpiServices.addRecommendation(request);
    }

    @PatchMapping("update_recommendation/{id}")
    public ResponseEntity<?>updateRecommendation(
            @RequestBody KPIRecommendationRequest request,
            @PathVariable Long id
            ) throws IOException {
        return adminKpiServices.updateRecommendation(request,id);
    }


    @GetMapping("get_recommendation/{intelRxId}/{kpiTypeId}")
    public ResponseEntity<?>getRecommendation(
            @PathVariable String intelRxId,
            @PathVariable Long kpiTypeId
    ){
        return adminKpiServices.getRecommendation(intelRxId,kpiTypeId);
    }

//    @GetMapping("get_recommendations/{intelRxId}")
//    public ResponseEntity<?>getAllRecommendation(
//            @PathVariable String intelRxId,
//            @PathVariable Long kpiTypeId
//    ){
//        return adminKpiServices.getAllRecommendation(intelRxId,kpiTypeId);
//    }

    @DeleteMapping("delete_recommendation")
    public ResponseEntity<?>deleteRecommendation(@RequestBody KPIRecommendationDeleteRequest request){
        return adminKpiServices.deleteRecommendation(request);
    }
}
