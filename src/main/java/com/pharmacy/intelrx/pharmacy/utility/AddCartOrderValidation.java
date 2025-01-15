package com.pharmacy.intelrx.pharmacy.utility;


import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.marketPlace.dto.MarketPlaceCartItemRequest;
import com.pharmacy.intelrx.pharmacy.dto.CartItemRequest;
import com.pharmacy.intelrx.pharmacy.dto.MedPrescriptionRequest;
import com.pharmacy.intelrx.pharmacy.dto.OrderRequest;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.TransferInventory;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PatientRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyRepository;
import com.pharmacy.intelrx.pharmacy.repositories.TransferInventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.RefillDurationRepository;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AddCartOrderValidation {
    private final Utility utility;
    private final PharmacyRepository pharmacyRepository;
    private final RefillDurationRepository refillDurationRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransferValidation inventoryTransferValidation;
    private final UserDetailsService userDetailsService;
    private final TransferInventoryRepository transferInventoryRepository;

    public ResponseEntity<?> validateCartItemRequest(CartItemRequest cartItemRequest) {

        if (cartItemRequest.getInventoryId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("inventoryId is required"));
        }

        if (cartItemRequest.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body(StandardResponse.error("quantity should be greater than zero"));
        }

        if (cartItemRequest.getVat() < 0) {
            return ResponseEntity.badRequest().body(StandardResponse.error("vat should not be negative"));
        }

        if (cartItemRequest.getAmount() <= 0) {
            return ResponseEntity.badRequest().body(StandardResponse.error("amount should be greater than zero"));
        }

//        if (cartItemRequest.getPatientId() == null) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("patientId is required"));
//        }

        // Additional validation logic if needed...
        boolean checkInventory = updateInventoryCheckQTY(cartItemRequest);
        if (!checkInventory) {
            int qtyLeft = checkInventoryQTY(cartItemRequest);
            return ResponseEntity.badRequest().body(StandardResponse.error("You have only " + qtyLeft + " quantity left"));
        }

        return ResponseEntity.ok(StandardResponse.success("Cart item validated successfully"));
    }

    public ResponseEntity<?> validateMedPrescriptionRequest(MedPrescriptionRequest request) {
        if (utility.isNullOrEmpty(request.getDosage())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("dosage is required"));
        }
        //if(request.getRefillDurationId())

        if (request.getRefillAlert().booleanValue()) {
            if (request.getRefillDurationId() == null || utility.isNullOrEmpty(String.valueOf(request.getRefillDurationId()))) {
                return ResponseEntity.badRequest().body(StandardResponse.error("refillDurationId is required"));
            }

            var refillDuration = refillDurationRepository.findById(request.getRefillDurationId());
            if (!refillDuration.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("refillDurationId is required or does not exist"));
            }
        }


        return ResponseEntity.ok(StandardResponse.success("Dosage validated successfully"));
    }

    public ResponseEntity<?> validateOrderRequest(OrderRequest orderRequest) {

        if (orderRequest.getTotalAmount() <= 0) {
            return ResponseEntity.badRequest().body(StandardResponse.error("totalAmount should be greater than zero"));
        }

        if (orderRequest.getPatientId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("patientId is required"));
        }

        // Additional validation logic if needed...

        return ResponseEntity.ok(StandardResponse.success("Order validated successfully"));
    }

    public boolean updateInventoryCheckQTY(CartItemRequest request) {
        var branch = userDetailsService.getBranch();
        if (branch == null) {
            //remove the quantity from the inventory
            Inventory inventory = inventoryTransferValidation.inventoryCheck(request.getInventoryId());

            if (inventory.getQuantity() > 0 && inventory.getQuantity() >= request.getQuantity()) {
                inventory.setQuantity(inventory.getQuantity() - request.getQuantity());
                inventoryRepository.save(inventory);
                return true;
            }
        } else {
            //remove the quantity from the inventory
            TransferInventory inventory = inventoryTransferValidation.branchInventoryCheck(request.getInventoryId());

            if (inventory.getQuantity() > 0 && inventory.getQuantity() >= request.getQuantity()) {
                inventory.setQuantity(inventory.getQuantity() - request.getQuantity());
                transferInventoryRepository.save(inventory);
                return true;
            }
        }

        return false;
    }

    public Integer checkInventoryQTY(CartItemRequest request) {
        var branch = userDetailsService.getBranch();
        if (branch == null) {
            Inventory inventory = inventoryTransferValidation.inventoryCheck(request.getInventoryId());

            // Check if the available quantity is less than the requested quantity
            return inventory.getQuantity();

        } else {
            TransferInventory inventory = inventoryTransferValidation.branchInventoryCheck(request.getInventoryId());

            // Check if the available quantity is less than the requested quantity
            return inventory.getQuantity();
        }
    }

    //Whole Seller Inventory Validation
    public ResponseEntity<?> validateMarketPlaceCartItemRequest(MarketPlaceCartItemRequest cartItemRequest) {

        if (cartItemRequest.getInventoryId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("inventoryId is required"));
        }

        if (cartItemRequest.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body(StandardResponse.error("quantity should be greater than zero"));
        }

        if (cartItemRequest.getVat() < 0) {
            return ResponseEntity.badRequest().body(StandardResponse.error("vat should not be negative"));
        }

        if (cartItemRequest.getAmount() <= 0) {
            return ResponseEntity.badRequest().body(StandardResponse.error("amount should be greater than zero"));
        }

        if (cartItemRequest.getSupplierPharmacyId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("supplierPharmacyId is required"));
        }

        Pharmacy supplierPharmacy = pharmacyRepository.findById(cartItemRequest.getSupplierPharmacyId()).orElse(null);
        var inventoryOptional = inventoryRepository.findByIdAndIntelRxId
                        (cartItemRequest.getInventoryId(), supplierPharmacy.getIntelRxId())
                .orElse(null);
        if (inventoryOptional == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Inventory with this id "
                    +cartItemRequest.getInventoryId()+" is not found"));
        }

        // Additional validation logic if needed...
        boolean checkInventory = updateMarketPlaceInventoryCheckQTY(cartItemRequest);
        if (!checkInventory) {
            int qtyLeft = checkMarketPlaceInventoryQTY(cartItemRequest);
            return ResponseEntity.badRequest().body(StandardResponse.error("You have only " + qtyLeft + " quantity left"));
        }

        return ResponseEntity.ok(StandardResponse.success("Cart item validated successfully"));
    }

    public boolean updateMarketPlaceInventoryCheckQTY(MarketPlaceCartItemRequest request) {
        Pharmacy supplierPharmacy = pharmacyRepository.findById(request.getSupplierPharmacyId()).orElse(null);
        assert supplierPharmacy != null;
        var intelRxId = supplierPharmacy.getIntelRxId();
        //remove the quantity from the inventory
        Inventory inventory = inventoryTransferValidation.inventoryCheckForWholesales(request.getInventoryId(),intelRxId);
        if (inventory.getWholeSaleQuantity() > 0 && inventory.getWholeSaleQuantity() >= request.getQuantity()) {
            inventory.setQuantity(inventory.getWholeSaleQuantity() - request.getQuantity());
            inventoryRepository.save(inventory);
            return true;
        }
        return false;
    }

    public Integer checkMarketPlaceInventoryQTY(MarketPlaceCartItemRequest request) {
        Pharmacy supplierPharmacy = pharmacyRepository.findById(request.getSupplierPharmacyId()).orElse(null);
        assert supplierPharmacy != null;
        var intelRxId = supplierPharmacy.getIntelRxId();
        Inventory inventory = inventoryTransferValidation.inventoryCheckForWholesales(request.getInventoryId(),intelRxId);
        // Check if the available quantity is less than the requested quantity
        return inventory.getWholeSaleQuantity();

    }

}
