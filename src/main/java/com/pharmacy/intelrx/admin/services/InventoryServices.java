package com.pharmacy.intelrx.admin.services;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.inventory.InventoryFilterRequest;
import com.pharmacy.intelrx.pharmacy.dto.inventory.InventoryRequest;
import com.pharmacy.intelrx.pharmacy.dto.inventory.InventoryResponse;
import com.pharmacy.intelrx.pharmacy.dto.inventory.OutOfStockResponse;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.models.orgSettings.DrugExpirationNotification;
import com.pharmacy.intelrx.pharmacy.models.orgSettings.OutOfStockNotification;
import com.pharmacy.intelrx.pharmacy.repositories.DrugExpirationNotificationRepository;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OutOfStockNotificationRepository;
import com.pharmacy.intelrx.pharmacy.utility.FilterInventory;
import com.pharmacy.intelrx.pharmacy.utility.InventoryTransferValidation;
import com.pharmacy.intelrx.pharmacy.utility.InventoryValidation;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("AdminPharmacyInventoryServices")
public class InventoryServices {
    private final InventoryRepository inventoryRepository;
    private final FilterInventory filterInventory;
    private final UserDetailsService userDetailsService;


    public ResponseEntity<?> fetchSingleInventory(Long inventoryId) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("You are unauthorized"));
        }

        if (inventoryId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("inventoryId is required"));
        }

        Optional<Inventory> optional = inventoryRepository.findById(inventoryId);
        Inventory inventory = optional.get();
        InventoryResponse responseList = filterInventory.mapToAddedInventoryResponse(inventory);

        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    public ResponseEntity<?> fetchAllInventory(InventoryFilterRequest request, Pageable pageable) {

        Page<Inventory> inventoryList = inventoryRepository.findByFilter(
                    request.getBrandId(),
                    request.getBrandClassId(),
                    request.getBrandFormId(),
                    request.getSizeId(),
                    request.getIntelRxId(),
                    request.getInventoryType(),
                    true,
                    request.getPoison(),
                    request.getSearchText(),
                    pageable
            );


        Page<InventoryResponse> responseList = inventoryList.map(inventory ->
                filterInventory.mapToAddedInventoryResponse(inventory));

        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    public ResponseEntity<?> fetchBranchInventory(InventoryFilterRequest request, Pageable pageable) {

        Page<Inventory> inventoryList = inventoryRepository.findByBranchFilter(
                request.getBrandId(),
                request.getBrandClassId(),
                request.getBrandFormId(),
                request.getSizeId(),
                "Received",
                request.getIntelRxId(),
                request.getInventoryType(),
                true,
                request.getPoison(),
                request.getSearchText(),
                request.getBranchId(),
                pageable
        );


        Page<InventoryResponse> responseList = inventoryList.map(inventory ->
                filterInventory.mapToAddedInventoryResponse(inventory));

        return ResponseEntity.ok(StandardResponse.success(responseList));
    }


}
