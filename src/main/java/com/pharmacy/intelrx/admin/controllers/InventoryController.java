package com.pharmacy.intelrx.admin.controllers;

import com.pharmacy.intelrx.admin.services.InventoryServices;
import com.pharmacy.intelrx.pharmacy.dto.inventory.InventoryFilterRequest;
import com.pharmacy.intelrx.pharmacy.dto.inventory.InventoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/admin/pharmacy"})
@RestController("AdminPharmacyInventoryController")
public class InventoryController {
    private final InventoryServices inventoryServices;


    @GetMapping("fetch_single_inventory/{inventoryId}")
    public ResponseEntity<?> fetchSingleInventory(@PathVariable Long inventoryId) {
        return inventoryServices.fetchSingleInventory(inventoryId);
    }

    @GetMapping({"fetch_all_inventory"})
    public ResponseEntity<?> fetchAllInventory(
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long brandClassId,
            @RequestParam(required = false) Long brandFormId,
            @RequestParam(required = false) Long sizeId,
            @RequestParam(required = false) String inventoryType,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) boolean poison,
            @RequestParam(required = true) String intelRxId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        InventoryFilterRequest request = new InventoryFilterRequest();
        request.setBrandId(brandId);
        request.setBrandClassId(brandClassId);
        request.setBrandFormId(brandFormId);
        request.setSizeId(sizeId);
        request.setIntelRxId(intelRxId);
        request.setInventoryType(inventoryType);
        request.setPoison(poison);
        request.setSearchText(searchText);

        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return inventoryServices.fetchAllInventory(request, pageable);
    }


    @GetMapping({"fetch_branch_inventory"})
    public ResponseEntity<?> fetchBranchInventory(
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long brandClassId,
            @RequestParam(required = false) Long brandFormId,
            @RequestParam(required = false) Long sizeId,
            @RequestParam(required = true) Long branchId,
            @RequestParam(required = false) String inventoryType,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) boolean poison,
            @RequestParam(required = true) String intelRxId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        InventoryFilterRequest request = new InventoryFilterRequest();
        request.setBrandId(brandId);
        request.setBrandClassId(brandClassId);
        request.setBrandFormId(brandFormId);
        request.setSizeId(sizeId);
        request.setBranchId(branchId);
        request.setIntelRxId(intelRxId);
        request.setInventoryType(inventoryType);
        request.setPoison(poison);
        request.setSearchText(searchText);

        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return inventoryServices.fetchBranchInventory(request, pageable);
    }



}
