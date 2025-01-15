package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.auxilliary.models.UserType;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.inventory.InventoryResponse;
import com.pharmacy.intelrx.pharmacy.dto.inventory.TransferInventoryRequest;
import com.pharmacy.intelrx.pharmacy.dto.inventory.TransferInventoryResponse;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.TransferInventory;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.repositories.TransferInventoryRepository;
import com.pharmacy.intelrx.pharmacy.utility.FilterEmployee;
import com.pharmacy.intelrx.pharmacy.utility.FilterInventory;
import com.pharmacy.intelrx.pharmacy.utility.InventoryTransferValidation;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyMapping;
import com.pharmacy.intelrx.utility.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("TransferInventoryService")
public class TransferInventoryService {
    private final TransferInventoryRepository transferInventoryRepository;
    private final InventoryTransferValidation transferValidation;
    private final UserDetailsService userDetailsService;
    private final FilterInventory filterInventory;
    private final PharmacyMapping pharmacyMapping;

    public ResponseEntity<?> Transfer(TransferInventoryRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("Un authorized"));
        }

        ResponseEntity response = transferValidation.validateTransferInventoryRequest(request);
        if (response.getStatusCode() != HttpStatus.OK) {
            return response;
        }

        PharmacyBranch transferFrom = transferValidation.branch(request.getTransferFromId());
        PharmacyBranch transferTo = transferValidation.branch(request.getTransferToId());
        Employee receivedBy = transferValidation.receivedBy(request.getReceivedById());
        Inventory inventory = transferValidation.inventoryCheck(request.getInventoryId());

        TransferInventory transferInventory = TransferInventory.builder().
                intelRxId(intelRxId).
                quantity(request.getQuantity())
                .inventory(inventory)
                .transferFrom(transferFrom == null ? null : transferFrom)
                .transferTo(transferTo == null ? null : transferTo)
                .receivedBy(receivedBy)
                .transferOfficer(user)
                .status("Pending").dateSent(LocalDate.now()).build();

        transferInventoryRepository.save(transferInventory);


        return ResponseEntity.ok(StandardResponse.success("The transfer personnel has been alerted via email, " +
                "The manager of the receiving branch will need to confirm receipt of the inventory for the transfer to be complete"));

    }

    public ResponseEntity<?> cancelTransfer(Long transferId) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("Un authorized"));
        }
        if (transferId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("transferId is required"));
        }

        Optional<TransferInventory> optionalTransferInventory = transferInventoryRepository.findByIntelRxIdAndId(intelRxId, transferId);
        if (!optionalTransferInventory.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("transferId does not exist"));
        }
        TransferInventory transferInventory = optionalTransferInventory.get();
        transferInventory.setStatus("Cancelled");
        transferInventoryRepository.save(transferInventory);

        return ResponseEntity.ok(StandardResponse.success("Cancelled Successfully"));
    }

    public ResponseEntity<?> confirmTransfer(Long transferId) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("Un authorized"));
        }
        if (transferId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("transferId is required"));
        }

        Optional<TransferInventory> optionalTransferInventory = transferInventoryRepository.findByIntelRxIdAndId(intelRxId, transferId);
        if (!optionalTransferInventory.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("transferId does not exist"));
        }
        TransferInventory transferInventory = optionalTransferInventory.get();
        transferInventory.setStatus("Received");
        transferInventoryRepository.save(transferInventory);

        return ResponseEntity.ok(StandardResponse.success("Received Successfully"));
    }

    public ResponseEntity<?> getTransferInventory(Long transferId) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("Un authorized"));
        }
        if (transferId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("transferId is required"));
        }

        Optional<TransferInventory> optionalTransferInventory = transferInventoryRepository.findByIntelRxIdAndId(intelRxId, transferId);
        if (!optionalTransferInventory.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("transferId does not exist"));
        }
        TransferInventory transferInventory = optionalTransferInventory.get();
        TransferInventoryResponse inventoryResponse = mapToTransferInventory(transferInventory);

        return ResponseEntity.ok(StandardResponse.success(inventoryResponse));
    }

    public ResponseEntity<?> getTransferInventories(Pageable pageable) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("Un authorized"));
        }
        Page<TransferInventory> transferInventoryList = null;
        if (branch == null && user.getUserType() == UserType.OWNER) {
            transferInventoryList = transferInventoryRepository.findAllByIntelRxId(intelRxId, pageable);
        }
        if (user.getUserType() == UserType.EMPLOYEE) {
            if(branch ==null){
                transferInventoryList = transferInventoryRepository.findAllByIntelRxId(intelRxId, pageable);
            }else{
                transferInventoryList = transferInventoryRepository.findByIntelRxIdAndTransferTo(intelRxId,branch, pageable);
            }
        }

        if (transferInventoryList == null || transferInventoryList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(transferInventoryList));
        }
        Page<TransferInventoryResponse> responseList = transferInventoryList.map(transferInventory ->
                mapToTransferInventory(transferInventory));

        return ResponseEntity.ok(StandardResponse.success(responseList));

    }

    public TransferInventoryResponse mapToTransferInventory(TransferInventory transferInventory) {

        TransferInventoryResponse transferInventoryResponse = new TransferInventoryResponse();
        transferInventoryResponse.setId(transferInventory.getId());
        transferInventoryResponse.setIntelRxId(transferInventory.getIntelRxId());
        transferInventoryResponse.setQuantity(transferInventory.getQuantity());
        transferInventoryResponse.setInventory(filterInventory.mapToTransferInventoryResponse(transferInventory));
        transferInventoryResponse.setDateSent(transferInventory.getDateSent());
        transferInventoryResponse.setReceivedTime(transferInventory.getReceivedTime());
        transferInventoryResponse.setDateSent(transferInventory.getDateSent());
        transferInventoryResponse.setTransferOfficer(userDetailsService.mapToUserInfo(transferInventory.getTransferOfficer()));
        transferInventoryResponse.setReceivedBy(userDetailsService.mapToUserInfo(transferInventory.getReceivedBy().getUser()));

        if (transferInventory.getTransferFrom() == null) {
            transferInventoryResponse.setTransferFrom("HeadQuarters");
        } else {
            transferInventoryResponse.setTransferFrom(pharmacyMapping.mapToTransferBranchResponse(transferInventory.getTransferFrom()));
        }
        transferInventoryResponse.setTransferTo(pharmacyMapping.mapToTransferBranchResponse(transferInventory.getTransferTo()));
        transferInventoryResponse.setStatus(transferInventory.getStatus());
        return transferInventoryResponse;
    }


}
