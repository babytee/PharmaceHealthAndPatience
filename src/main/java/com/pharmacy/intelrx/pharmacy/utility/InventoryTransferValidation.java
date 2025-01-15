package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.PatientRequest;
import com.pharmacy.intelrx.pharmacy.dto.inventory.TransferInventoryRequest;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.TransferInventory;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyBranchRepository;
import com.pharmacy.intelrx.pharmacy.repositories.TransferInventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InventoryTransferValidation {
    private final Utility utility;
    private final TransferInventoryRepository transferInventoryRepository;
    private final UserDetailsService userDetailsService;
    private final PharmacyBranchRepository branchRepository;
    private final EmployeeRepository employeeRepository;
    private final InventoryRepository inventoryRepository;

    public ResponseEntity<?> validateTransferInventoryRequest(TransferInventoryRequest request) {

        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();

        if (request.getQuantity() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("quantity is required"));
        }

        if (request.getInventoryId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("inventoryId is required"));
        }

        if (request.getTransferToId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("transferToId is required"));
        }


//        if (request.getTransferFromId() == null) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("transferFromId is required"));
//        }

        if (request.getReceivedById() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("receivedById is required"));
        }

        if (branch(request.getTransferToId()) == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("This pharmacy branch does not exist"));
        }

        if (receivedBy(request.getReceivedById()) == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Employee to oversee does not exist"));
        }

        if (checkReceiverAsBranchManager(request.getTransferToId(), request.getReceivedById()) == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("The receiver is not a manager in this branch"));
        }

        if (inventoryCheck(request.getInventoryId()) == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("This inventory does not exist"));
        }

        if (!checkInventoryQty(request.getInventoryId(), request.getQuantity())) {
            Inventory inventory = inventoryCheck(request.getInventoryId());
            String msg = "The available quantity is " + inventory.getQuantity()+" while your request quantity is "+request.getQuantity();
            return ResponseEntity.badRequest().body(StandardResponse.error(msg));
        }

        //remove the quantity from the inventory
        Inventory inventory = inventoryCheck(request.getInventoryId());
        inventory.setQuantity(inventory.getQuantity() - request.getQuantity());
        inventoryRepository.save(inventory);
        // Additional validation logic if needed...

        return ResponseEntity.ok(StandardResponse.success("TransferInventory request validated successfully"));
    }

    public ResponseEntity<?> validateUpdateTransferInventoryRequest(TransferInventoryRequest request, Long transferId) {
        if (transferId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("transferId is required"));
        }

        var transfer = transferInventoryRepository.findById(transferId);
        if (!transfer.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("transferId does not exist"));
        }

        if (request.getQuantity() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("quantity is required"));
        }

        if (request.getInventoryId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("inventoryId is required"));
        }

        if (request.getTransferToId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("transferToId is required"));
        }

//        if (request.getTransferFromId() == null) {
//            return ResponseEntity.badRequest().body(StandardResponse.error("transferFromId is required"));
//        }

        if (request.getReceivedById() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("receivedById is required"));
        }

        if (branch(request.getTransferToId()) == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("This pharmacy branch does not exist"));
        }

        if (receivedBy(request.getReceivedById()) == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Employee to oversee does not exist"));
        }

        if (inventoryCheck(request.getInventoryId()) == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("This inventory does not exist"));
        }


        // Additional validation logic if needed...

        return ResponseEntity.ok(StandardResponse.success("TransferInventory request validated successfully"));
    }

    public PharmacyBranch branch(Long branchId) {
        var intelRxId = userDetailsService.getIntelRxId();
        var branchOptional = branchRepository.findByIdAndIntelRxId(branchId, intelRxId);
        if (!branchOptional.isPresent()) {
            return null;
        }
        return branchOptional.get();
    }

    public Employee receivedBy(Long receivedById) {
        var intelRxId = userDetailsService.getIntelRxId();
        var employeeOptional = employeeRepository.findByIdAndEmployeeIntelRxId(receivedById, intelRxId);
        if (!employeeOptional.isPresent()) {
            return null;
        }
        return employeeOptional.get();
    }

    public PharmacyBranch checkReceiverAsBranchManager(Long branchId, Long receivedById) {
        var intelRxId = userDetailsService.getIntelRxId();
        var branchOptional = branchRepository.findByIdAndIntelRxIdAndEmployeeId(branchId, intelRxId, receivedById);
        if (!branchOptional.isPresent()) {
            return null;
        }
        return branchOptional.get();
    }

    public Inventory inventoryCheck(Long inventoryId) {
        var intelRxId = userDetailsService.getIntelRxId();
        var inventoryOptional = inventoryRepository.findByIdAndIntelRxId(inventoryId, intelRxId)
                .orElse(null);
        if (inventoryOptional == null) {
            return null;
        }
        return inventoryOptional;
    }

    public Inventory inventoryCheckForWholesales(Long inventoryId,String intelRxId) {
        var inventoryOptional = inventoryRepository.findByIdAndIntelRxId(inventoryId, intelRxId)
                .orElse(null);
        if (inventoryOptional == null) {
            return null;
        }
        return inventoryOptional;
    }

    public TransferInventory branchInventoryCheck(Long inventoryId) {
        var intelRxId = userDetailsService.getIntelRxId();
        var inventoryOptional = transferInventoryRepository.findByIntelRxIdAndId(intelRxId,inventoryId);
        if (!inventoryOptional.isPresent()) {
            return null;
        }
        return inventoryOptional.get();
    }

    public boolean checkInventoryQty(Long inventoryId, Integer quantity) {
        var intelRxId = userDetailsService.getIntelRxId();
        var inventoryOptional = inventoryRepository.findByIdAndIntelRxId(inventoryId, intelRxId);

        if (inventoryOptional.isPresent()) {
            Inventory inventory = inventoryOptional.get();

            // Check if the available quantity is less than the requested quantity
            return inventory.getQuantity() >= quantity || inventory.getWholeSaleQuantity() >= quantity;
        }
        return false;
    }

}
