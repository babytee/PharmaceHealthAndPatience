package com.pharmacy.intelrx.auxilliary.kpiCalculation.RetailFinancial;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CalculateDIO {
    private final GeneralCalculation generalCalculation;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    public double getCalculateDIO(String intelRxId, String duration) {
        /**
         DIO=(Average Inventory/Cost of Goods Sold)×365

         In your case, we will assume:

         Average Inventory: This can be derived from the Inventory entity by averaging the inventory value over a given period.
         Cost of Goods Sold (COGS): This can be derived from the Order and Inventory entities.
         Steps to Implement DIO Calculation

         Average Inventory: Average of costPrice * quantity from the Inventory entity.
         COGS: Sum of costPrice * quantity from Order entity.
         */

        LocalDateTime startDate = generalCalculation.calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

        Double averageInventory = inventoryRepository.getAverageInventoryValue(startDate, endDate, intelRxId);
        Double totalCOGS = orderRepository.getTotalCOGS(startDate, endDate, intelRxId);

        if (averageInventory == null) {
            averageInventory = 0.0;
        }
        if (totalCOGS == null) {
            totalCOGS = 0.0;
        }

        if (totalCOGS == 0) {
            return 0.0; // Avoid division by zero
        }

        return (averageInventory / totalCOGS) * 365;
    }

}
