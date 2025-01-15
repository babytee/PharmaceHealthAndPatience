package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.auxilliary.models.InventoryDictionary;
import com.pharmacy.intelrx.auxilliary.models.PaymentMethod;
import com.pharmacy.intelrx.auxilliary.models.PaymentStatus;
import com.pharmacy.intelrx.auxilliary.repositories.InventoryDictionaryRepository;
import com.pharmacy.intelrx.auxilliary.repositories.PaymentMethodRepository;
import com.pharmacy.intelrx.auxilliary.repositories.PaymentStatusRepository;
import com.pharmacy.intelrx.brand.models.Brand;
import com.pharmacy.intelrx.brand.models.BrandClass;
import com.pharmacy.intelrx.brand.models.BrandForm;
import com.pharmacy.intelrx.brand.models.Size;
import com.pharmacy.intelrx.brand.repositories.BrandClassRepository;
import com.pharmacy.intelrx.brand.repositories.BrandFormRepository;
import com.pharmacy.intelrx.brand.repositories.BrandRepository;
import com.pharmacy.intelrx.brand.repositories.SizeRepository;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.inventory.InventoryRequest;
import com.pharmacy.intelrx.pharmacy.dto.inventory.SupplierPaymentRequest;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class InventoryValidation {
    private final Utility utility;
    private final BrandFormRepository brandFormRepository;
    private final BrandClassRepository brandClassRepository;
    private final BrandRepository brandRepository;
    private final SizeRepository sizeRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryDictionaryRepository dictionaryRepository;

    public ResponseEntity<?> validateInventoryRequest(InventoryRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Request cannot be null"));
        }
        if (request.getInventoryType().equals("MEDICATION")) {
            ResponseEntity responseEntity = validateInventoryMedication(request);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                return responseEntity;
            }
        } else if (request.getInventoryType().equals("GROCERY")) {
            ResponseEntity responseEntity = validateInventoryGrocery(request);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                return responseEntity;
            }
        } else {
            var inv = inventoryRepository.findByItemNameAndInventoryType(request.getItemName(), request.getInventoryType());
            if (inv.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("itemName Already Exist"));
            }

        }
        // If all validations pass, return success response or proceed with other logic.
        return ResponseEntity.ok(StandardResponse.success("Validation successful"));
    }

    public ResponseEntity<?> validateUpdateInventoryRequest(InventoryRequest request, Long inventoryId) {
        if (request == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Request cannot be null"));
        }
        if (inventoryId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("inventoryId cannot be null"));
        }
        if (request.getInventoryType().equals("MEDICATION")) {
            ResponseEntity responseEntity = validateInventoryMedication(request);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                return responseEntity;
            }
        } else if (request.getInventoryType().equals("GROCERY")) {
            ResponseEntity responseEntity = validateInventoryGrocery(request);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                return responseEntity;
            }
        }
        // If all validations pass, return success response or proceed with other logic.
        return ResponseEntity.ok(StandardResponse.success("Validation successful"));
    }

    public ResponseEntity<?> validateInventoryMedication(InventoryRequest request) {
        if (utility.isNullOrEmpty(request.getInventoryType())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("inventoryType"));
        }
        else if (utility.isNullOrEmpty(request.getItemName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("itemName"));
        } else if (utility.isNullOrEmpty(request.getBrandId()) & utility.isNullOrEmpty(request.getBrandName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("brandId or brandName is empty"));
        } else if (request.getSizeId() == null & utility.isNullOrEmpty(request.getSizeName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("sizeId or sizeName is empty"));
        } else if (request.getQuantity() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("quantity"));
        } else if (request.getCostPrice() < 1) {
            return ResponseEntity.badRequest().body(StandardResponse.error("costPrice"));
        } else if (request.getSalePrice() < 1) {
            return ResponseEntity.badRequest().body(StandardResponse.error("salePrice"));
        } else if (request.getExpDay() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("expDay"));
        } else if (request.getExpMonth() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("expMonth"));
        } else if (request.getExpYear() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("expYear"));
        }
//        else if (request.getPoison() == null) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("poison"));
//        }
        else {
//            var size = addSizeIfNotExists(request.getSizeId());
//            var brand = addBrandIfNotExists(request.getBrandId());
//            var brandForm = addBrandFormIfNotExists(request.getBrandFormId());
//
//            Optional<Inventory> optional = inventoryRepository.findByIntelRxIdAndInventoryTypeAndItemNameAndBrandAndBrandFormAndSize(
//                    request.getIntelRxId(),
//                    "MEDICATION",
//                    request.getItemName(),
//                    brand, brandForm, size
//            );
//
//            if (optional.isPresent()) {
//                return ResponseEntity.badRequest().body(StandardResponse.error("itemName with this brand,form and size already existed."));
//            }

            // If all validations pass, return success response or proceed with other logic.
            return ResponseEntity.ok(StandardResponse.success("Validation successful"));
        }
    }

    public ResponseEntity<?> validateInventoryGrocery(InventoryRequest request) {
        if (utility.isNullOrEmpty(request.getInventoryType())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("inventoryType"));
        } else if (utility.isNullOrEmpty(request.getItemName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("itemName"));
        } else if (request.getSizeId() == null & utility.isNullOrEmpty(request.getSizeName())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("sizeId or sizeName is empty"));
        } else if (request.getQuantity() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("quantity"));
        } else if (request.getCostPrice() < 1) {
            return ResponseEntity.badRequest().body(StandardResponse.error("costPrice"));
        } else if (request.getSalePrice() < 1) {
            return ResponseEntity.badRequest().body(StandardResponse.error("salePrice"));
        } else if (request.getExpDay() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("expDay"));
        } else if (request.getExpMonth() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("expMonth"));
        } else if (request.getExpYear() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("expYear"));
        } else {

            // If all validations pass, return success response or proceed with other logic.
            return ResponseEntity.ok(StandardResponse.success("Validation successful"));
        }
    }

    public void addInventoryItemIfNotExists(String itemName, String itemType) {
        Optional<InventoryDictionary> optional = dictionaryRepository.findByItemName(itemName);
        InventoryDictionary inventoryDictionary = new InventoryDictionary();
        if (optional == null || optional.isEmpty()) {
            inventoryDictionary.setInventoryType(itemType);
            inventoryDictionary.setItemName(itemName);
            dictionaryRepository.save(inventoryDictionary);
        }
    }

    public Size addSizeIfNotExists(Long medicationSizeLabel) {
        Optional<Size> optional = sizeRepository.findById(medicationSizeLabel);
        Size size;
        if (optional == null || optional.isEmpty()) {
            size = null;
        } else {
            size = optional.get();
        }
        return size;
    }


    public Brand addBrandIfNotExists(String brandLabel) {
        if (brandLabel == null || brandLabel.isEmpty()) {
            // If brandLabel is null or empty, return null
            return null;
        }

        try {
            // Try to parse brandLabel as a Long
            Long id = Long.parseLong(brandLabel);
            // If successful, check if a brand with this ID exists
            Optional<Brand> optional = brandRepository.findById(id);

            if (optional.isPresent()) {
                // If the brand is found in the repository, return it
                return optional.get();
            }
            return null;
        } catch (NumberFormatException e) {
            // If brandLabel is not a valid Long, assume it's a brand name
            Optional<Brand> optional = brandRepository.findByName(brandLabel);

            if (optional.isPresent()) {
                // If the brand is found by name, return it
                return optional.get();
            } else {
                // If the brand is not found, create a new Brand with the given name and save it
                Brand newBrand = new Brand();
                newBrand.setName(brandLabel);  // Assuming you have a setName method
                return brandRepository.save(newBrand);
            }
        }
    }


    public BrandClass addBrandClassIfNotExists(String brandClassLabel) {
        if (brandClassLabel == null || brandClassLabel.isEmpty()) {
            // If brandClassLabel is null or empty, return null
            return null;
        }

        try {
            // Try to parse brandClassLabel as a Long
            Long id = Long.parseLong(brandClassLabel);
            // If successful, check if a brand class with this ID exists
            Optional<BrandClass> optional = brandClassRepository.findById(id);

            if (optional.isPresent()) {
                // If the brand class is found in the repository, return it
                return optional.get();
            }
            return null;
        } catch (NumberFormatException e) {
            // If brandClassLabel is not a valid Long, assume it's a brand class name
            Optional<BrandClass> optional = brandClassRepository.findByName(brandClassLabel);

            if (optional.isPresent()) {
                // If the brand class is found by name, return it
                return optional.get();
            } else {
                // If the brand class is not found, create a new BrandClass with the given name and save it
                BrandClass newBrandClass = new BrandClass();
                newBrandClass.setName(brandClassLabel);  // Assuming you have a setName method
                return brandClassRepository.save(newBrandClass);
            }
        }
    }


    public BrandForm addBrandFormIfNotExists(String brandFormLabel) {
        if (brandFormLabel == null || brandFormLabel.isEmpty()) {
            // If brandFormLabel is null or empty, return null
            return null;
        }

        try {
            // Try to parse brandFormLabel as a Long
            Long id = Long.parseLong(brandFormLabel);
            // If successful, check if a brand form with this ID exists
            Optional<BrandForm> optional = brandFormRepository.findById(id);

            if (optional.isPresent()) {
                // If the brand form is found in the repository, return it
                return optional.get();
            }
            return null;
        } catch (NumberFormatException e) {
            // If brandFormLabel is not a valid Long, assume it's a brand form name
            Optional<BrandForm> optional = brandFormRepository.findByName(brandFormLabel);

            if (optional.isPresent()) {
                // If the brand form is found by name, return it
                return optional.get();
            } else {
                // If the brand form is not found, create a new BrandForm with the given name and save it
                BrandForm newBrandForm = new BrandForm();
                newBrandForm.setName(brandFormLabel);  // Assuming you have a setName method
                return brandFormRepository.save(newBrandForm);
            }
        }
    }


    public ResponseEntity<?> validateSupplierRequest(SupplierPaymentRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("SupplierRequest cannot be null"));
        } else if (request.getSupplierId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("supplierId is required"));
        } else if (request.getPaymentStatusId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("PaymentStatusId is required"));
        } else if (request.getPaymentMethodId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("PaymentMethodId is required"));
        } else if (request.getAmountPaid() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("AmountPaid is required"));
        } else if (request.getPaymentStatusId() == 2) {
            if (request.getBalanceDue() == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("BalanceDue is required"));
            } else if (request.getDueDay() == null || request.getDueMonth() == null || request.getDueYear() == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Due date components (day, month, year) are required"));
            }
        }
//        else if (utility.isNullOrEmpty(request.getPurchaseInvoice()) && utility.isNullOrEmpty(request.getBankTransfer())) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("Either PurchaseInvoice or BankTransfer is required"));
//        }
        // If all validations pass, return success response or proceed with other logic.
        return ResponseEntity.ok(StandardResponse.success("Validation successful"));
    }

    public ResponseEntity<?> validateUpdateSupplierRequest(SupplierPaymentRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("SupplierRequest cannot be null"));
        } else if (request.getAmountPaid() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("AmountPaid is required"));
        } else if (request.getBalanceDue() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("BalanceDue is required"));
        } else {

            // If all validations pass, return success response or proceed with other logic.
            return ResponseEntity.ok(StandardResponse.success("Validation successful"));
        }
    }

    public PaymentMethod checkPaymentMethodIfNotExists(Long paymentMethodId) {
        Optional<PaymentMethod> optional = paymentMethodRepository.findById(paymentMethodId);
        PaymentMethod size = null;
        if (optional.isEmpty()) {
            size = null;
        } else {
            size = optional.get();
        }
        return size;
    }

    public PaymentStatus checkPaymentStatusIfNotExists(Long paymentStatusId) {
        Optional<PaymentStatus> optional = paymentStatusRepository.findById(paymentStatusId);
        PaymentStatus paymentStatus = null;
        if (optional.isEmpty()) {
            paymentStatus = null;
        } else {
            paymentStatus = optional.get();
        }
        return paymentStatus;
    }


}
