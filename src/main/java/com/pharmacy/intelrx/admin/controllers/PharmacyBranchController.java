package com.pharmacy.intelrx.admin.controllers;

import com.pharmacy.intelrx.admin.services.PharmacyBranchService;
import com.pharmacy.intelrx.pharmacy.dto.PharmacyBranchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/admin/pharmacy/branch"})
@RestController("AdminPharmacyBranchController")
public class PharmacyBranchController {
    private final PharmacyBranchService pharmacyBranchService;


    @GetMapping({"all/{intelRxId}"})
    public  ResponseEntity<?>getAllBranch(@PathVariable String intelRxId){
        return pharmacyBranchService.getAllBranch(intelRxId);
    }

    @GetMapping({"/{branchId}/{intelRxId}"})
    public  ResponseEntity<?>getBranch(
            @PathVariable Long branchId,
            @PathVariable String intelRxId){
        return pharmacyBranchService.getBranch(branchId,intelRxId);
    }

    @GetMapping({"branch_stats/{branchId}/{intelRxId}"})
    public  ResponseEntity<?>branchOverviewStats(
            @PathVariable Long branchId,
            @PathVariable String intelRxId){
        return pharmacyBranchService.branchOverviewStats(branchId,intelRxId);
    }

}
