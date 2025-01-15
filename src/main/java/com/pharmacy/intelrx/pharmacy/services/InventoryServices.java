package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.models.UserType;
import com.pharmacy.intelrx.brand.models.Brand;
import com.pharmacy.intelrx.brand.models.BrandClass;
import com.pharmacy.intelrx.brand.models.BrandForm;
import com.pharmacy.intelrx.brand.models.Size;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.config.DictionariesInitializer;
import com.pharmacy.intelrx.pharmacy.dto.inventory.*;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.models.TransferInventory;
import com.pharmacy.intelrx.pharmacy.models.orgSettings.DrugExpirationNotification;
import com.pharmacy.intelrx.pharmacy.models.orgSettings.OutOfStockNotification;
import com.pharmacy.intelrx.pharmacy.models.orgSettings.SalesMargin;
import com.pharmacy.intelrx.pharmacy.repositories.*;
import com.pharmacy.intelrx.pharmacy.utility.FilterInventory;
import com.pharmacy.intelrx.pharmacy.utility.InventoryValidation;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("PharmacyInventoryServices")
public class InventoryServices {
    private final InventoryValidation inventoryValidation;
    private final InventoryRepository inventoryRepository;
    private final Utility utility;
    private final FilterInventory filterInventory;
    private final UserDetailsService userDetailsService;
    private final OutOfStockNotificationRepository outOfStockNotificationRepository;
    private final DrugExpirationNotificationRepository drugExpirationNotificationRepository;
    private final TransferInventoryRepository transferInventoryRepository;
    private final SupplierService supplierService;
    private final DictionariesInitializer dictionariesInitializer;
    private final SalesMarginRepository salesMarginRepository;


    @Transactional
    public ResponseEntity<?> addNewInventory(InventoryRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }
        Inventory inventory = new Inventory();

        ResponseEntity<?> responseEntity = inventoryValidation.validateInventoryRequest(request);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return inventoryValidation.validateInventoryRequest(request);
        }


        if (request.getInventoryType().equals("MEDICATION")) {
            var size = inventoryValidation.addSizeIfNotExists(request.getSizeId());
            var brand = inventoryValidation.addBrandIfNotExists(request.getBrandId());
            var brandForm = inventoryValidation.addBrandFormIfNotExists(request.getBrandFormId());

            Inventory inventory1 = inventoryRepository.findByIntelRxIdAndInventoryTypeAndItemNameAndBrandAndBrandFormAndSize(
                    intelRxId,
                    "MEDICATION",
                    request.getItemName(),
                    brand, brandForm, size
            ).orElse(null);

            if (inventory1 != null) {
                inventory = addMedication(request, user, inventory1.getId(), "");
            } else {
                inventory = addMedication(request, user, null, "");
            }
        }

        if (request.getInventoryType().equals("GROCERY")) {
            Inventory inventory1 = inventoryRepository.findByItemNameAndIntelRxId
                    (request.getItemName(), intelRxId).orElse(null);
            if (inventory1 != null) {
                inventory = addGrocery(request, user, inventory1.getId(), "");
            } else {
                inventory = addGrocery(request, user, null, "");
            }
        }
        //inventoryRepository.save(inventory);
        // Save inventory if created
        if (inventory != null) {
            inventoryRepository.save(inventory);
        }


        return ResponseEntity.ok().body(StandardResponse.success("Inventory Added Successfully"));

    }

    @Transactional
    public ResponseEntity<?> uploadInventoryByCSV(MultipartFile file) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Uploaded file is empty"));
        }

        List<String> errors = new ArrayList<>();
        int successCount = 0;
        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            // Parse CSV file using Apache Commons CSV
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreEmptyLines()
                    .parse(reader);

            List<Inventory> inventoryBatch = new ArrayList<>();
            for (CSVRecord record : records) {
                try {
                    //get sales margin to calculate the cost margin
                    SalesMargin salesMargin = salesMarginRepository.findByIntelRxId(intelRxId).orElse(null);
                    String inventoryType = record.get("inventoryType").trim();

                    // Map CSV headers to InventoryRequest fields
                    InventoryRequest request = new InventoryRequest();
                    request.setInventoryType(record.get("inventoryType").trim());
                    request.setBarCodeNumber(record.get("barCodeNumber").trim());
                    request.setItemName(record.get("itemName").trim());
                    request.setBrandName(record.get("brandName").trim());
                    request.setBrandClassName(record.get("brandClassName").trim());
                    request.setBrandFormName(record.get("brandFormName").trim());
                    request.setSizeName(record.get("sizeName").trim());
                    request.setQuantity(Integer.parseInt(record.get("quantity").trim()));
                    request.setCostPrice(Double.parseDouble(record.get("costPrice").trim()));
                    Double salesPrice = Double.parseDouble(
                            record.get("salePrice").trim());

                    Double costPrice = Double.parseDouble(
                            record.get("costPrice").trim());

                    if(inventoryType.equals("MEDICATION")){
                        request.setSalePrice(salesPrice != null || salesPrice > 0
                                ? salesPrice : salesMargin.getMedicationMargin() * costPrice);
                    }
                    if(inventoryType.equals("GROCERY")){
                        request.setSalePrice(salesPrice != null || salesPrice > 0
                                ? salesPrice : salesMargin.getGroceryMargin() * costPrice);
                    }

                    request.setWholeSalePrice(Double.parseDouble(record.get("wholeSalePrice").trim()));
                    request.setWholeSaleQuantity(Integer.parseInt(record.get("wholeSaleQuantity").trim()));
                    request.setExpDay(Integer.parseInt(record.get("expDay").trim()));
                    request.setExpMonth(Integer.parseInt(record.get("expMonth").trim()));
                    request.setExpYear(Integer.parseInt(record.get("expYear").trim()));
                    request.setPoison(Boolean.parseBoolean(record.get("poison").trim()));

                    // Validate inventory request
//                    ResponseEntity<?> validationResponse = inventoryValidation.validateInventoryRequest(request);
//                    if (validationResponse.getStatusCode() != HttpStatus.OK) {
//                        errors.add("Error in row " + record.getRecordNumber() + ": " + validationResponse.getBody());
//                        continue;
//                    }

                    // Add inventory based on type
                    Inventory inventory = processInventory(request, user, intelRxId);
                    if (inventory != null) {
                        inventoryBatch.add(inventory);
                        successCount++;
                    }

                    // Batch save every 1000 records
                    if (inventoryBatch.size() == 1000) {
                        inventoryRepository.saveAll(inventoryBatch);
                        inventoryBatch.clear();
                    }
                } catch (Exception e) {
                    errors.add("Error in row " + record.getRecordNumber()+1 + ": " + e.getMessage());
                }
            }

            // Save remaining records in the batch
            if (!inventoryBatch.isEmpty()) {
                inventoryRepository.saveAll(inventoryBatch);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StandardResponse.error("Error reading the uploaded file"));
        }

        return ResponseEntity.ok().body(
                StandardResponse.success(
                        successCount + " inventories added successfully. " +
                                (errors.isEmpty() ? "" : "Errors: " + errors)
                )
        );
    }

    // Helper method to process inventory by type
    private Inventory processInventory(InventoryRequest request, User user, String intelRxId) {
        Inventory inventory = null;

        if ("MEDICATION".equalsIgnoreCase(request.getInventoryType())) {
            var size = dictionariesInitializer.addSizeIfNotExists(request.getSizeName());
            var brand = dictionariesInitializer.addBrandIfNotExists(request.getBrandName());
            var brandForm = dictionariesInitializer.addFormIfNotExists(request.getBrandFormName());

            Inventory existingInventory = inventoryRepository.findByIntelRxIdAndInventoryTypeAndItemNameAndBrandAndBrandFormAndSize(
                    intelRxId, "MEDICATION", request.getItemName(), brand, brandForm, size).orElse(null);

            inventory = existingInventory != null
                    ? addMedication(request, user, existingInventory.getId(), "csv")
                    : addMedication(request, user, null, "csv");

        } else if ("GROCERY".equalsIgnoreCase(request.getInventoryType())) {
            Inventory existingInventory = inventoryRepository.findByItemNameAndIntelRxId(
                    request.getItemName(), "GROCERY").orElse(null);

            inventory = existingInventory != null
                    ? addGrocery(request, user, existingInventory.getId(), "csv")
                    : addGrocery(request, user, null, "csv");
        }

        return inventory;
    }

    @Transactional
    public ResponseEntity<?> updateInventory(InventoryRequest request, Long inventoryId) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }

        ResponseEntity<?> responseEntity = inventoryValidation.validateUpdateInventoryRequest(request, inventoryId);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return inventoryValidation.validateInventoryRequest(request);
        }

        Inventory inventory = new Inventory();
        if (request.getInventoryType().equals("MEDICATION")) {
            inventory = addMedication(request, user, inventoryId, "");
        }

        if (request.getInventoryType().equals("GROCERY")) {
            inventory = addGrocery(request, user, inventoryId, "");
        }

        inventoryRepository.save(inventory);


        return ResponseEntity.ok().body(StandardResponse.success("Inventory Added Successfully"));

    }

    public Inventory addMedication(InventoryRequest request, User user, Long inventoryId, String uploadType) {
        Size size = null;
        Brand brand = null;
        BrandForm brandForm = null;
        BrandClass brandClass = null;

        if (uploadType.equals("csv")) {
            size = dictionariesInitializer.addSizeIfNotExists(request.getSizeName());
            brand = dictionariesInitializer.addBrandIfNotExists(request.getBrandName());
            brandForm = dictionariesInitializer.addFormIfNotExists(request.getBrandFormName());
            brandClass = dictionariesInitializer.addClassIfNotExists(request.getBrandClassName());

        } else {
            size = inventoryValidation.addSizeIfNotExists(request.getSizeId());
            brand = inventoryValidation.addBrandIfNotExists(request.getBrandId());
            brandForm = inventoryValidation.addBrandFormIfNotExists(request.getBrandFormId());
            brandClass = inventoryValidation.addBrandClassIfNotExists(request.getBrandClassId());
        }


        //add new inventory item name to dictionary if not existing
        inventoryValidation.addInventoryItemIfNotExists(request.getItemName(), request.getInventoryType());

        String ref = "";
        Inventory inventory = null;

        if (inventoryId == null) {
            //ref = utility.generateUniqueInvoiceRefNumber();
            inventory = new Inventory();
        } else {
            inventory = inventoryRepository.findById(inventoryId).orElse(null);

            if (inventory == null) {
                inventory = new Inventory();
            }
        }

        String barCodeNumber = (request.getBarCodeNumber() != null) ? request.getBarCodeNumber() : "";
        double wholeSalePrice = (request.getWholeSalePrice() > 0) ? request.getWholeSalePrice() : 0;
        int wholeSaleQuantity = (request.getWholeSaleQuantity() > 0) ? request.getWholeSaleQuantity() : 0;

        boolean poison = (request.getPoison()) ? request.getPoison() : false;

        String intelRxId = null;
        if (request.getIntelRxId() != null) {
            intelRxId = request.getIntelRxId();
        } else {
            intelRxId = userDetailsService.getIntelRxId();
        }

        if (request.getWholeSaleQuantity() > 0) {
            //if the user is a wholesaler, add him or her to the supplier list
            var userInfo = userDetailsService.mapToUserInfo(user);
            SupplierRequest supplierRequest = new SupplierRequest();
            supplierRequest.setName(userInfo.getFirstName());
            supplierRequest.setPhoneNumber(userInfo.getPhoneNumber());
            supplierRequest.setUser(user);
            supplierRequest.setIntelRxId(intelRxId);
            supplierService.newSupplier(supplierRequest, null);
        }

        inventory.setUser(user);
        inventory.setIntelRxId(intelRxId);
        inventory.setInventoryType(request.getInventoryType());
        inventory.setBarCodeNumber(barCodeNumber);
        inventory.setItemName(request.getItemName());
        inventory.setBrand(brand);
        inventory.setBrandClass(brandClass);
        inventory.setBrandForm(brandForm);
        inventory.setSize(size);
        inventory.setWholeSalePrice(wholeSalePrice);
        inventory.setWholeSaleQuantity(wholeSaleQuantity);
        inventory.setQuantity(request.getQuantity());
        inventory.setCostPrice(request.getCostPrice());
        inventory.setSalePrice(request.getSalePrice());
        inventory.setExpDay(request.getExpDay());
        inventory.setExpMonth(request.getExpMonth());
        inventory.setExpYear(request.getExpYear());
        inventory.setPoison(poison);
        //inventory.setInvoiceRefNumber(ref);
        inventory.setCreatedAt(LocalDateTime.now());
        return inventory;

    }

    public Inventory addGrocery(InventoryRequest request, User user, Long inventoryId, String uploadType) {
        Size size = null;
        if (uploadType.equals("csv")) {
            size = dictionariesInitializer.addSizeIfNotExists(request.getSizeName());
        } else {
            size = inventoryValidation.addSizeIfNotExists(request.getSizeId());
        }

        //add new inventory item name to dictionary if not existing
        inventoryValidation.addInventoryItemIfNotExists(request.getItemName(), request.getInventoryType());

        //String ref = "";
        Inventory inventory = null;
        if (inventoryId == null) {
            //ref = utility.generateUniqueInvoiceRefNumber();
            inventory = new Inventory();
        } else {
            Optional<Inventory> optionalInventory = inventoryRepository.findById(inventoryId);

            if (optionalInventory.isPresent()) {
                inventory = optionalInventory.get();

            } else {
                inventory = new Inventory();
            }
        }

        String barCodeNumber = (request.getBarCodeNumber() != null) ? request.getBarCodeNumber() : "";
        double wholeSalePrice = (request.getWholeSalePrice() > 0) ? request.getWholeSalePrice() : 0;
        int wholeSaleQuantity = (request.getWholeSaleQuantity() > 0) ? request.getWholeSaleQuantity() : 0;

        boolean poison = (request.getPoison()) ? request.getPoison() : false;
        String intelRxId = null;
        if (request.getIntelRxId() != null) {
            intelRxId = request.getIntelRxId();
        } else {
            intelRxId = userDetailsService.getIntelRxId();
        }

        if (request.getWholeSaleQuantity() > 0) {
            //if the user is a wholesaler, add him or her to the supplier list
            var userInfo = userDetailsService.mapToUserInfo(user);
            SupplierRequest supplierRequest = new SupplierRequest();
            supplierRequest.setName(userInfo.getFirstName());
            supplierRequest.setPhoneNumber(userInfo.getPhoneNumber());
            supplierRequest.setUser(user);
            supplierRequest.setIntelRxId(intelRxId);
            supplierService.newSupplier(supplierRequest, null);
        }


        inventory.setUser(user);
        inventory.setIntelRxId(intelRxId);
        inventory.setInventoryType(request.getInventoryType());
        inventory.setBarCodeNumber(barCodeNumber);
        inventory.setItemName(request.getItemName());
        inventory.setSize(size);
        inventory.setQuantity(request.getQuantity());
        inventory.setCostPrice(request.getCostPrice());
        inventory.setSalePrice(request.getSalePrice());
        inventory.setWholeSalePrice(wholeSalePrice);
        inventory.setWholeSaleQuantity(wholeSaleQuantity);
        inventory.setExpDay(request.getExpDay());
        inventory.setExpMonth(request.getExpMonth());
        inventory.setExpYear(request.getExpYear());
        inventory.setPoison(poison);
        //inventory.setInvoiceRefNumber(ref);
        inventory.setCreatedAt(LocalDateTime.now());

        return inventory;
    }

    public ResponseEntity<?> fetchAddedInventory() {
        var user = userDetailsService.getAuthenticatedUser();

        if (user == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }

        var intelRxId = userDetailsService.getIntelRxId();

        List<Inventory> inventoryList = inventoryRepository.findAllByIntelRxIdAndUserAndStatus(intelRxId, user, false);
        List<InventoryResponse> responseList = inventoryList.stream().map(inventory ->
                filterInventory.mapToAddedInventoryResponse(inventory)).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    public ResponseEntity<?> fetchSingleInventory(Long inventoryId) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }

        if (inventoryId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("inventoryId is required"));
        }

        String intelRxId = userDetailsService.getIntelRxId();

        Optional<Inventory> optional = inventoryRepository.findByIdAndIntelRxId(inventoryId, intelRxId);
        Inventory inventory = optional.get();
        InventoryResponse responseList = filterInventory.mapToAddedInventoryResponse(inventory);

        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    @Transactional
    public ResponseEntity<?> removeInventory(Long inventoryId) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }

        if (inventoryId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("inventoryId is required"));
        }

        String intelRxId = userDetailsService.getIntelRxId();

        Inventory inventory = inventoryRepository.findByIdAndIntelRxId(inventoryId, intelRxId)
                .orElse(null);
        if (inventory == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("item not found"));
        }

        inventory.setDeleteStatus(true);
        inventoryRepository.save(inventory);

        return ResponseEntity.ok(StandardResponse.success(inventory.getItemName() + " removed"));
    }

    public ResponseEntity<?> deleteInventory(Long inventoryId) {

        var user = userDetailsService.getAuthenticatedUser();

        if (user == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }

        if (inventoryId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("inventoryId is required"));
        }

        String intelRxId = userDetailsService.getIntelRxId();

        Inventory inventory = inventoryRepository.findByIdAndIntelRxId(inventoryId, intelRxId)
                .orElse(null);

        if (inventory == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("item not found"));
        }

        inventoryRepository.delete(inventory);

        return ResponseEntity.ok(StandardResponse.success(inventory.getItemName() + " removed"));
    }

    public ResponseEntity<?> fetchAllInventory(InventoryFilterRequest request, Pageable pageable) {

        var user = userDetailsService.getAuthenticatedUser();
        var branch = userDetailsService.getBranch();

        if (user == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }

        var intelRxId = userDetailsService.getIntelRxId();

        Page<Inventory> inventoryList = null;
        if (branch == null && user.getUserType() == UserType.OWNER) {
            inventoryList = inventoryRepository.findByFilter(
                    request.getBrandId(),
                    request.getBrandClassId(),
                    request.getBrandFormId(),
                    request.getSizeId(),
                    intelRxId,
                    request.getInventoryType(),
                    true,
                    request.getPoison(),
                    request.getSearchText(),
                    pageable
            );
        }
        if (user.getUserType() == UserType.EMPLOYEE) {
            if (branch == null) {
                inventoryList = inventoryRepository.findByFilter(
                        request.getBrandId(),
                        request.getBrandClassId(),
                        request.getBrandFormId(),
                        request.getSizeId(),
                        intelRxId,
                        request.getInventoryType(),
                        true,
                        false,
                        request.getSearchText(),
                        pageable
                );
            } else {
                inventoryList = inventoryRepository.findByBranchFilter(
                        request.getBrandId(),
                        request.getBrandClassId(),
                        request.getBrandFormId(),
                        request.getSizeId(),
                        "Received",
                        intelRxId,
                        request.getInventoryType(),
                        true,
                        request.getPoison(),
                        request.getSearchText(),
                        branch.getId(),
                        pageable
                );
            }
        }

        Page<InventoryResponse> responseList = inventoryList.map(inventory ->
                filterInventory.mapToAddedInventoryResponse(inventory));

        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    public ResponseEntity<?> fetchAllByInventoryType(String inventoryType) {
        String intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        List<Inventory> inventoryList = null;
        List<TransferInventory> transferInventoryList = null;
        List<InventoryResponse> responseList = null;

        if (branch == null) {
            if (utility.isNullOrEmpty(inventoryType)) {
                inventoryList = inventoryRepository.findAllByIntelRxIdAndStatus(intelRxId, true);
            } else {
                inventoryList = inventoryRepository.findAllByIntelRxIdAndInventoryTypeAndStatus(intelRxId, inventoryType, true);
            }
            responseList = inventoryList.stream().map(inventory ->
                    filterInventory.mapToAddedInventoryResponse(inventory)).collect(Collectors.toList());

        } else {
            transferInventoryList = transferInventoryRepository.findAllByIntelRxIdAndTransferTo(intelRxId, branch);

            responseList = transferInventoryList.stream().map(inventory ->
                    filterInventory.mapToTransferInventoryResponse(inventory)).collect(Collectors.toList());
        }


        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    public ResponseEntity<?> fetchInventoryQtyAssessValues() {
        String intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        List<Inventory> inventoryList = null;
        List<TransferInventory> transferInventoryList = null;
        Map<String, String> responseList = new HashMap<>();
        double inventoryMedicationSum = 0.0;
        double inventoryGrocerySum = 0.0;

        int medicationQty = 0;
        int groceryQty = 0;

        if (branch == null) {
            inventoryList = inventoryRepository.findAllByIntelRxIdAndStatus(intelRxId, true);

            for (Inventory inventory : inventoryList) {
                if (inventory.getInventoryType().equals("MEDICATION")
                        || inventory.getInventoryType() == "MEDICATION") {
                    inventoryMedicationSum += inventory.getSalePrice() * inventory.getQuantity();
                    medicationQty++;
                }

                if (inventory.getInventoryType().equals("GROCERY")
                        || inventory.getInventoryType() == "GROCERY") {
                    inventoryGrocerySum += inventory.getSalePrice() * inventory.getQuantity();
                    groceryQty++;
                }
            }
        } else {
            transferInventoryList = transferInventoryRepository.findAllByIntelRxIdAndTransferTo(intelRxId, branch);
            for (TransferInventory transferInventory : transferInventoryList) {
                Inventory inventory = transferInventory.getInventory();
                if (inventory.getInventoryType().equals("MEDICATION")
                        || inventory.getInventoryType() == "MEDICATION") {
                    inventoryMedicationSum += inventory.getSalePrice() * transferInventory.getQuantity();
                    medicationQty++;
                }

                if (inventory.getInventoryType().equals("GROCERY")
                        || inventory.getInventoryType() == "GROCERY") {
                    inventoryGrocerySum += inventory.getSalePrice() * transferInventory.getQuantity();
                    groceryQty++;
                }
            }
        }

        responseList.put("medicationSum", String.valueOf(utility.roundAmount(inventoryMedicationSum)));
        responseList.put("medicationQty", String.valueOf(medicationQty));
        responseList.put("grocerySum", String.valueOf(utility.roundAmount(inventoryGrocerySum)));
        responseList.put("groceryQty", String.valueOf(groceryQty));


        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    public ResponseEntity<?> outOfStock(String inventoryType) {
        String intelRxId = userDetailsService.getIntelRxId();
        OutOfStockNotification outOfStockNotification = outOfStockNotificationRepository.
                findByIntelRxId(intelRxId).orElse(null);

        if (outOfStockNotification == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("No Out Of Stock Expiration has been set"));
        }

        List<Inventory> inventoryList;
        if (utility.isNullOrEmpty(inventoryType)) {
            inventoryList = inventoryRepository.findAllByIntelRxIdAndStatus(intelRxId, true);
        } else {
            inventoryList = inventoryRepository.findAllByIntelRxIdAndInventoryTypeAndStatus
                    (intelRxId, inventoryType, true);
        }

        List<OutOfStockResponse> responseList = inventoryList.stream().map(inventory -> {
            OutOfStockResponse response = filterInventory.mapToOutOfStockResponse(inventory, outOfStockNotification);
            if (response.getInventoryId() == null) {
                System.out.println("Empty response for inventory ID: " + inventory.getId());
            }
            return response;
        }).collect(Collectors.toList());

        // Remove empty responses
        responseList = responseList.stream()
                .filter(response -> response.getInventoryId() != null)
                .collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    public ResponseEntity<?> drugExpiration() {
        String intelRxId = userDetailsService.getIntelRxId();
        Optional<DrugExpirationNotification> optional = drugExpirationNotificationRepository.findByIntelRxId(intelRxId);

        if (!optional.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("No Drug Expiration Notification has been set"));
        }

        DrugExpirationNotification drugExpirationNotification = optional.get();
        List<Inventory> inventoryList = inventoryRepository.findAllByIntelRxIdAndStatus(intelRxId, true);

        List<OutOfStockResponse> responseList = inventoryList.stream().map(inventory -> {
                    OutOfStockResponse response = filterInventory.mapToDrugExpirationResponse(inventory, drugExpirationNotification);
                    if (response != null && response.getInventoryId() == null) {
                        System.out.println("Empty response for inventory ID: " + inventory.getId());
                    }
                    return response;
                }).filter(Objects::nonNull) // Filter out null responses
                .collect(Collectors.toList());

        // Remove empty responses with null inventory IDs
        responseList = responseList.stream()
                .filter(response -> response.getInventoryId() != null)
                .collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(responseList));
    }


}
