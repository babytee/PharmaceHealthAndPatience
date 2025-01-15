package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.pharmacy.dto.PharmacyBranchRequest;
import com.pharmacy.intelrx.pharmacy.services.PharmacyBranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy/branch"})
@RestController("PharmacyBranchController")
public class PharmacyBranchController {
    private final PharmacyBranchService pharmacyBranchService;

    @PostMapping({"add_new"})
    public ResponseEntity<?> addNewBranch(@RequestBody PharmacyBranchRequest request) {
        return pharmacyBranchService.addNewBranch(request);
    }

    @PatchMapping({"update/{id}"})
    public ResponseEntity<?> updateBranch(@RequestBody PharmacyBranchRequest request, @PathVariable Long id) {
        return pharmacyBranchService.updateBranch(request,id);
    }

    @GetMapping({"get_all"})
    public  ResponseEntity<?>getAllBranch(){
        return pharmacyBranchService.getAllBranch();
    }

    @GetMapping({"/{branchId}"})
    public  ResponseEntity<?>getBranch(@PathVariable Long branchId){
        return pharmacyBranchService.getBranch(branchId);
    }
}
