package com.pharmacy.intelrx.admin.services;

import com.pharmacy.intelrx.admin.dto.DictionaryRequest;
import com.pharmacy.intelrx.auxilliary.models.InventoryDictionary;
import com.pharmacy.intelrx.auxilliary.repositories.InventoryDictionaryRepository;
import com.pharmacy.intelrx.brand.models.Brand;
import com.pharmacy.intelrx.brand.models.BrandClass;
import com.pharmacy.intelrx.brand.models.BrandForm;
import com.pharmacy.intelrx.brand.models.Size;
import com.pharmacy.intelrx.brand.repositories.BrandClassRepository;
import com.pharmacy.intelrx.brand.repositories.BrandFormRepository;
import com.pharmacy.intelrx.brand.repositories.BrandRepository;
import com.pharmacy.intelrx.brand.repositories.SizeRepository;
import com.pharmacy.intelrx.common.StandardResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service("DictionaryManagementService")
public class DictionaryManagementService {
    private final BrandRepository brandRepository;
    private final SizeRepository sizeRepository;
    private final BrandFormRepository brandFormRepository;
    private final BrandClassRepository brandClassRepository;
    private final InventoryDictionaryRepository inventoryDictionaryRepository;

    //private final AdminDetailsService adminDetailsService;

    public ResponseEntity<?> dictionaryStart() {
        List<InventoryDictionary> dictionaries = inventoryDictionaryRepository.findAll();
        List<BrandForm> brandForms = brandFormRepository.findAll();
        List<Brand> brands = brandRepository.findAll();
        List<BrandClass> brandClasses = brandClassRepository.findAll();

        HashMap<String, Integer> stringMap = new HashMap<>();
        stringMap.put("totalBrands", brands.size());
        stringMap.put("totalForms", brandForms.size());
        stringMap.put("totalClass", brandClasses.size());
        stringMap.put("totalGenericNames", dictionaries.size());

        return ResponseEntity.ok(StandardResponse.success(stringMap));
    }

    public ResponseEntity<?> addMedToDictionary(DictionaryRequest request) {
        Optional<InventoryDictionary> optional = inventoryDictionaryRepository.findByItemName(request.getName());
        if (optional.isEmpty()) {
            InventoryDictionary inventoryDictionary = new InventoryDictionary();
            inventoryDictionary.setItemName(request.getName());
            inventoryDictionary.setInventoryType(request.getMedType());
            inventoryDictionaryRepository.save(inventoryDictionary);
        } else {
            return ResponseEntity.ok(StandardResponse.success(
                    request.getName() + " Already Existed"));
        }

        return ResponseEntity.ok(StandardResponse.success("Added Successfully"));
    }

    public ResponseEntity<?> updateMedToDictionary(DictionaryRequest request, Long id) {
        Optional<InventoryDictionary> optional = inventoryDictionaryRepository.findById(id);
        if (optional.isPresent()) {
            InventoryDictionary inventoryDictionary = optional.get();
            inventoryDictionary.setItemName(request.getName());
            inventoryDictionary.setInventoryType(request.getMedType());
            inventoryDictionaryRepository.save(inventoryDictionary);
        } else {
            return ResponseEntity.ok(StandardResponse.success(
                    "BrandClass with id " + id + " does not exist"));
        }
        return ResponseEntity.ok(StandardResponse.success("Updated Successfully"));
    }

    // Method to search with filters and pagination
    public Page<InventoryDictionary> searchInventory(String itemName, String inventoryType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return inventoryDictionaryRepository.searchInventory(itemName, inventoryType, pageable);
    }

    public ResponseEntity<?> addBrandClass(DictionaryRequest request) {
        Optional<BrandClass> optional = brandClassRepository.findByName(request.getName());
        if (optional.isEmpty()) {
            BrandClass brandClass = new BrandClass();
            brandClass.setName(request.getName());
            brandClassRepository.save(brandClass);
        } else {
            return ResponseEntity.ok(StandardResponse.success(
                    request.getName() + " Already Existed"));
        }

        return ResponseEntity.ok(StandardResponse.success("Added Successfully"));
    }

    public ResponseEntity<?> updateBrandClass(DictionaryRequest request, Long id) {
        Optional<BrandClass> optional = brandClassRepository.findById(id);
        if (optional.isPresent()) {
            BrandClass brandClass = optional.get();
            brandClass.setName(request.getName());
            brandClassRepository.save(brandClass);
        } else {
            return ResponseEntity.ok(StandardResponse.success(
                    "BrandClass with id " + id + " does not exist"));
        }
        return ResponseEntity.ok(StandardResponse.success("Updated Successfully"));
    }

    // Method to search with filters and pagination
    public Page<BrandClass> searchBrandClass(String itemName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return brandClassRepository.searchBrandClass(itemName, pageable);
    }


    public ResponseEntity<?> BrandForm(DictionaryRequest request) {
        Optional<BrandForm> optional = brandFormRepository.findByName(request.getName());
        if (optional.isEmpty()) {
            BrandForm brandForm = new BrandForm();
            brandForm.setName(request.getName());
            brandFormRepository.save(brandForm);
        } else {
            return ResponseEntity.ok(StandardResponse.success(
                    request.getName() + " Already Existed"));
        }

        return ResponseEntity.ok(StandardResponse.success("Added Successfully"));
    }

    public ResponseEntity<?> updateBrandForm(DictionaryRequest request, Long id) {
        Optional<BrandForm> optional = brandFormRepository.findById(id);
        if (optional.isPresent()) {
            BrandForm brandForm = optional.get();
            brandForm.setName(request.getName());
            brandFormRepository.save(brandForm);
        } else {
            return ResponseEntity.ok(StandardResponse.success(
                    "BrandForm with id " + " does not exist"));
        }
        return ResponseEntity.ok(StandardResponse.success("Updated Successfully"));
    }

    // Method to search with filters and pagination
    public Page<BrandForm> searchBrandForm(String itemName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return brandFormRepository.searchBrandForm(itemName, pageable);
    }

    public ResponseEntity<?> addSize(DictionaryRequest request) {
        Optional<Size> optional = sizeRepository.findByName(request.getName());
        if (optional.isEmpty()) {
            Size size = new Size();
            size.setName(request.getName());
            sizeRepository.save(size);
        } else {
            return ResponseEntity.ok(StandardResponse.success(
                    request.getName() + " Already Existed"));
        }

        return ResponseEntity.ok(StandardResponse.success("Added Successfully"));
    }

    public ResponseEntity<?> updateSize(DictionaryRequest request, Long id) {
        Optional<Size> optional = sizeRepository.findById(id);
        if (optional.isPresent()) {
            Size size = optional.get();
            size.setName(request.getName());
            sizeRepository.save(size);
        } else {
            return ResponseEntity.ok(StandardResponse.success(
                    "BrandForm with id " + " does not exist"));
        }
        return ResponseEntity.ok(StandardResponse.success("Updated Successfully"));
    }

    // Method to search with filters and pagination
    public Page<Size> searchSize(String itemName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return sizeRepository.searchSize(itemName, pageable);
    }

    public ResponseEntity<?> addBrand(DictionaryRequest request) {
        Optional<Brand> optionalBrand = brandRepository.findByName(request.getName());

        if (optionalBrand.isEmpty()) {
            Brand brand = new Brand();
            brand.setName(request.getName());
            brandRepository.save(brand);
        } else {
            return ResponseEntity.ok(StandardResponse.success(
                    request.getName() + " Already Existed"));
        }

        return ResponseEntity.ok(StandardResponse.success("Added Successfully"));
    }

    public ResponseEntity<?> updateBrand(DictionaryRequest request, Long id) {
        Optional<Brand> optional = brandRepository.findById(id);
        if (optional.isPresent()) {
            Brand brand = optional.get();
            brand.setName(request.getName());
            brandRepository.save(brand);
        } else {
            return ResponseEntity.ok(StandardResponse.success(
                    "BrandForm with id " + id + " does not exist"));
        }
        return ResponseEntity.ok(StandardResponse.success("Updated Successfully"));
    }

    // Method to search with filters and pagination
    public Page<Brand> searchBrand(String itemName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return brandRepository.searchBrand(itemName, pageable);
    }

    // Method to handle CSV upload for InventoryDictionary
    public ResponseEntity<?> addMedToDictionaryCsv(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            for (CSVRecord record : csvParser) {
                String name = record.get("name");
                String medType = record.get("medType");

                Optional<InventoryDictionary> optional = inventoryDictionaryRepository.findByItemName(name);
                if (optional.isEmpty()) {
                    InventoryDictionary inventoryDictionary = new InventoryDictionary();
                    inventoryDictionary.setItemName(name);
                    inventoryDictionary.setInventoryType(medType);
                    inventoryDictionaryRepository.save(inventoryDictionary);
                }
            }
        }

        return ResponseEntity.ok(StandardResponse.success("Added Successfully"));
    }

    // Method to handle CSV upload for BrandClass
    public ResponseEntity<?> addBrandClassCsv(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            for (CSVRecord record : csvParser) {
                String name = record.get("name");

                Optional<BrandClass> optional = brandClassRepository.findByName(name);
                if (optional.isEmpty()) {
                    BrandClass brandClass = new BrandClass();
                    brandClass.setName(name);
                    brandClassRepository.save(brandClass);
                }
            }
        }

        return ResponseEntity.ok(StandardResponse.success("Added Successfully"));
    }

    // Method to handle CSV upload for BrandForm
    public ResponseEntity<?> addMedFormCsv(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            for (CSVRecord record : csvParser) {
                String name = record.get("name");

                Optional<BrandForm> optional = brandFormRepository.findByName(name);
                if (optional.isEmpty()) {
                    BrandForm brandForm = new BrandForm();
                    brandForm.setName(name);
                    brandFormRepository.save(brandForm);
                }
            }
        }

        return ResponseEntity.ok(StandardResponse.success("Added Successfully"));
    }

    // Method to handle CSV upload for Size
    public ResponseEntity<?> addSizeCsv(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            for (CSVRecord record : csvParser) {
                String name = record.get("name");

                Optional<BrandClass> optional = brandClassRepository.findByName(name);
                if (optional.isEmpty()) {
                    BrandClass brandClass = new BrandClass();
                    brandClass.setName(name);
                    brandClassRepository.save(brandClass);
                }
            }
        }

        return ResponseEntity.ok(StandardResponse.success("Added Successfully"));
    }

    // Method to handle CSV upload for Brand
    public ResponseEntity<?> addBrandCsv(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            for (CSVRecord record : csvParser) {
                String name = record.get("name");

                Optional<Brand> optional = brandRepository.findByName(name);
                if (optional.isEmpty()) {
                    Brand brand = new Brand();
                    brand.setName(name);
                    brandRepository.save(brand);
                }
            }
        }

        return ResponseEntity.ok(StandardResponse.success("Added Successfully"));
    }

}
