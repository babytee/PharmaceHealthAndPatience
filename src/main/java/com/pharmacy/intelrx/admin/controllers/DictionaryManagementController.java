package com.pharmacy.intelrx.admin.controllers;

import com.pharmacy.intelrx.admin.dto.DictionaryRequest;
import com.pharmacy.intelrx.admin.services.DictionaryManagementService;
import com.pharmacy.intelrx.auxilliary.models.InventoryDictionary;
import com.pharmacy.intelrx.brand.models.Brand;
import com.pharmacy.intelrx.brand.models.BrandClass;
import com.pharmacy.intelrx.brand.models.BrandForm;
import com.pharmacy.intelrx.brand.models.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/admin/dictionary"})
@RestController("DictionaryManagementController")
public class DictionaryManagementController {
    private final DictionaryManagementService dictionaryService;


    @GetMapping("overview_stats")
    public ResponseEntity<?> dictionaryStart() {
        return dictionaryService.dictionaryStart();
    }

    // Endpoint to add InventoryDictionary
    @PostMapping("add_med_to_dictionary")
    public ResponseEntity<?> addMedToDictionary(@RequestBody DictionaryRequest request) {
        return dictionaryService.addMedToDictionary(request);
    }

    // Endpoint to update InventoryDictionary
    @PatchMapping("update_med_to_dictionary/{id}")
    public ResponseEntity<?> updateMedToDictionary(@RequestBody DictionaryRequest request, @PathVariable Long id) {
        return dictionaryService.updateMedToDictionary(request, id);
    }

    @GetMapping("/search-inventory")
    public ResponseEntity<Page<InventoryDictionary>> searchInventory(
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String inventoryType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<InventoryDictionary> result = dictionaryService.searchInventory(itemName, inventoryType, page, size);
        return ResponseEntity.ok(result);
    }

    // Endpoint to add BrandClass
    @PostMapping("add_brand_class")
    public ResponseEntity<?> addBrandClass(@RequestBody DictionaryRequest request) {
        return dictionaryService.addBrandClass(request);
    }

    // Endpoint to update BrandClass
    @PatchMapping("update_brand_class/{id}")
    public ResponseEntity<?> updateBrandClass(@RequestBody DictionaryRequest request, @PathVariable Long id) {
        return dictionaryService.updateBrandClass(request, id);
    }

    @GetMapping("/search-brand-class")
    public ResponseEntity<Page<BrandClass>> searchInventory(
            @RequestParam(required = false) String itemName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<BrandClass> result = dictionaryService.searchBrandClass(itemName, page, size);
        return ResponseEntity.ok(result);
    }

    // Endpoint to add BrandForm
    @PostMapping("add_brand_form")
    public ResponseEntity<?> BrandForm(@RequestBody DictionaryRequest request) {
        return dictionaryService.BrandForm(request);
    }

    // Endpoint to update BrandForm
    @PatchMapping("update_brand_form/{id}")
    public ResponseEntity<?> updateBrandForm(@RequestBody DictionaryRequest request, @PathVariable Long id) {
        return dictionaryService.updateBrandForm(request, id);
    }

    @GetMapping("/search-brand-form")
    public ResponseEntity<Page<BrandForm>> searchBrandForm(
            @RequestParam(required = false) String itemName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<BrandForm> result = dictionaryService.searchBrandForm(itemName, page, size);
        return ResponseEntity.ok(result);
    }

    // Endpoint to add Size
    @PostMapping("add_size")
    public ResponseEntity<?> addSize(@RequestBody DictionaryRequest request) {
        return dictionaryService.addSize(request);
    }

    // Endpoint to update Size
    @PatchMapping("update_size/{id}")
    public ResponseEntity<?> updateSize(@RequestBody DictionaryRequest request, @PathVariable Long id) {
        return dictionaryService.updateSize(request, id);
    }

    @GetMapping("/search-size")
    public ResponseEntity<Page<Size>> searchSize(
            @RequestParam(required = false) String itemName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<Size> result = dictionaryService.searchSize(itemName, page, size);
        return ResponseEntity.ok(result);
    }

    // Endpoint to add Brand
    @PostMapping("add_brand")
    public ResponseEntity<?> addBrand(@RequestBody DictionaryRequest request) {
        return dictionaryService.addBrand(request);
    }

    // Endpoint to update Brand
    @PatchMapping("update_brand/{id}")
    public ResponseEntity<?> updateBrand(@RequestBody DictionaryRequest request, @PathVariable Long id) {
        return dictionaryService.updateBrand(request, id);
    }

    @GetMapping("/search-brand")
    public ResponseEntity<Page<Brand>> searchBrand(
            @RequestParam(required = false) String itemName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<Brand> result = dictionaryService.searchBrand(itemName, page, size);
        return ResponseEntity.ok(result);
    }


    // Endpoint to upload CSV for InventoryDictionary
    @PostMapping("/med-to-dictionary/upload")
    public ResponseEntity<?> uploadMedToDictionaryCsv(@RequestParam("file") MultipartFile file) throws IOException {
        return dictionaryService.addMedToDictionaryCsv(file);
    }

    // Endpoint to upload CSV for BrandClass
    @PostMapping("/brand-class/upload")
    public ResponseEntity<?> uploadBrandClassCsv(@RequestParam("file") MultipartFile file) throws IOException {
        return dictionaryService.addBrandClassCsv(file);
    }

    // Endpoint to upload CSV for BrandForm
    @PostMapping("/med-form/upload")
    public ResponseEntity<?> uploadMedFormCsv(@RequestParam("file") MultipartFile file) throws IOException {
        return dictionaryService.addMedFormCsv(file);
    }

    // Endpoint to upload CSV for Size
    @PostMapping("/size/upload")
    public ResponseEntity<?> uploadSizeCsv(@RequestParam("file") MultipartFile file) throws IOException {
        return dictionaryService.addSizeCsv(file);
    }

    // Endpoint to upload CSV for Brand
    @PostMapping("/brand/upload")
    public ResponseEntity<?> uploadBrandCsv(@RequestParam("file") MultipartFile file) throws IOException {
        return dictionaryService.addBrandCsv(file);
    }
}
