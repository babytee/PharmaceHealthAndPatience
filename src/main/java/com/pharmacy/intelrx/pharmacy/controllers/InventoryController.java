package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.inventory.InventoryFilterRequest;
import com.pharmacy.intelrx.pharmacy.dto.inventory.InventoryRequest;
import com.pharmacy.intelrx.pharmacy.services.InventoryServices;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy"})
@RestController("PharmacyInventoryController")
public class InventoryController {
    private final InventoryServices inventoryServices;

    @PostMapping("add_new_inventory")
    public ResponseEntity<?> addNewInventory(@RequestBody InventoryRequest request) {
        return inventoryServices.addNewInventory(request);
    }

    @PostMapping("/upload_new_inventory_csv")
    public ResponseEntity<?> uploadInventoryCSV(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("File is missing or empty."));
        }
        return inventoryServices.uploadInventoryByCSV(file);
    }

    @PatchMapping("update_inventory/{inventoryId}")
    public ResponseEntity<?> updateInventory(@RequestBody InventoryRequest request, @PathVariable Long inventoryId) {
        return inventoryServices.updateInventory(request, inventoryId);
    }

    @GetMapping("fetch_single_inventory/{inventoryId}")
    public ResponseEntity<?> fetchSingleInventory(@PathVariable Long inventoryId) {
        return inventoryServices.fetchSingleInventory(inventoryId);
    }

    @GetMapping({"fetch_added_inventory"})
    public ResponseEntity<?> fetchAddedInventory() {
        return inventoryServices.fetchAddedInventory();
    }

    @DeleteMapping({"remove_add_inventory/{inventoryId}"})
    public ResponseEntity<?> removeInventory(@PathVariable Long inventoryId) {
        return inventoryServices.removeInventory(inventoryId);
    }

    @DeleteMapping({"delete_inventory/{inventoryId}"})
    public ResponseEntity<?> deleteInventory(@PathVariable Long inventoryId) {
        return inventoryServices.deleteInventory(inventoryId);
    }

    @GetMapping({"fetch_all_inventory"})
    public ResponseEntity<?> fetchAllInventory(
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long brandClassId,
            @RequestParam(required = false) Long brandFormId,
            @RequestParam(required = false) Long sizeId,
            @RequestParam(required = false) String inventoryType,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) Boolean poison,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {

        InventoryFilterRequest request = new InventoryFilterRequest();
        request.setBrandId(brandId);
        request.setBrandClassId(brandClassId);
        request.setBrandFormId(brandFormId);
        request.setSizeId(sizeId);
        request.setInventoryType(inventoryType);
        request.setSearchText(searchText);
        request.setPoison(poison);

        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return inventoryServices.fetchAllInventory(request, pageable);
    }

    @GetMapping({"fetch_by_inventory_type"})
    public ResponseEntity<?> fetchAllByInventoryType(@RequestParam(required = false) String inventoryType) {
        return inventoryServices.fetchAllByInventoryType(inventoryType);
    }

    @GetMapping({"inventory_stat"})
    public ResponseEntity<?> inventoryQtyAssessValuesStat() {
        return inventoryServices.fetchInventoryQtyAssessValues();
    }

    @GetMapping({"out_of_stock"})
    public ResponseEntity<?> outOfStock(@RequestParam(required = true) String inventoryType) {
        return inventoryServices.outOfStock(inventoryType);
    }

    @GetMapping({"drug_expiration"})
    public ResponseEntity<?> drugExpiration() {
        return inventoryServices.drugExpiration();
    }
}
