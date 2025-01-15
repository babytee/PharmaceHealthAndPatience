package com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.pharmacy.models.CartItem;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CalculateSellThrough {

    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final GeneralCalculation generalCalculation;

    public double calculateSellThroughRate(String intelRxId, String duration) {
/**
 Sell-Through Rate=(Number of Units Sold/Number of Units Received)×100
 */
// Determine start and end dates based on the provided duration
        LocalDateTime startDate = generalCalculation.calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now(); // End date is the current date
        Integer beginningInventory = inventoryRepository.getBeginningInventoryQuantity(startDate, intelRxId);
        Integer purchasesDuringPeriod = inventoryRepository.getPurchasesQuantityDuringPeriod(startDate, endDate, intelRxId);
        Integer unitsSold = orderRepository.getUnitsSold(startDate, endDate, intelRxId);

        if (beginningInventory == null) {
            beginningInventory = 0;
        }
        if (purchasesDuringPeriod == null) {
            purchasesDuringPeriod = 0;
        }
        if (unitsSold == null) {
            unitsSold = 0;
        }

        int totalUnitsReceived = beginningInventory + purchasesDuringPeriod;

        if (totalUnitsReceived == 0) {
            return 0.0; // Avoid division by zero
        }

        return ((double) unitsSold / totalUnitsReceived) * 100;

    }


}
