package com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.pharmacy.models.CartItem;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CalculateShrinkage {
    private final InventoryRepository inventoryRepository;

    public double calculateShrinkage(String intelRxId, String duration) {
        /**
         Shrinkage=((Recorded Inventory − Actual Inventory)/Recorded Inventory)×100
         **/
        Integer recordedInventory = inventoryRepository.getRecordedInventoryQuantity(intelRxId);
        Integer actualInventory = inventoryRepository.getActualInventoryQuantity(intelRxId);

        if (recordedInventory == null || recordedInventory == 0) {
            return 0.0; // Avoid division by zero
        }
        if (actualInventory == null) {
            actualInventory = 0;
        }

        return ((double) (recordedInventory - actualInventory) / recordedInventory) * 100;

    }

}
