package com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CalculateCostOfGoodsSold {
    private final InventoryRepository inventoryRepository;

    public double calculateCOGS(String intelRxId, String duration) {
        /**  The basic formula for calculating COGS is:

         COGS=Beginning Inventory+Purchases during the period−Ending Inventory

         Explanation of Components
         Beginning Inventory: The value of the inventory at the start of the accounting period.
         Purchases during the period: The total cost of inventory items purchased during the accounting period.
         Ending Inventory: The value of the inventory at the end of the accounting period.
         **/

        LocalDateTime startDate = calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

        Double beginningInventoryValue = inventoryRepository.getBeginningInventoryValue(startDate, intelRxId);
        Double purchasesValueDuringPeriod = inventoryRepository.getPurchasesValueDuringPeriod(startDate, endDate, intelRxId);
        Double endingInventoryValue = inventoryRepository.getEndingInventoryValue(endDate, intelRxId);

        // Handle null values
        beginningInventoryValue = (beginningInventoryValue != null) ? beginningInventoryValue : 0.0;
        purchasesValueDuringPeriod = (purchasesValueDuringPeriod != null) ? purchasesValueDuringPeriod : 0.0;
        endingInventoryValue = (endingInventoryValue != null) ? endingInventoryValue : 0.0;

        // Calculate COGS
        return beginningInventoryValue + purchasesValueDuringPeriod - endingInventoryValue;

    }

    private LocalDateTime calculateStartDateBasedOnDuration(String duration) {
        switch (duration) {
            case "30 Days":
                return LocalDateTime.now().minusDays(30);
            case "3 Months":
                return LocalDateTime.now().minusMonths(3);
            case "6 Months":
                return LocalDateTime.now().minusMonths(6);
            case "1 Year":
                return LocalDateTime.now().minusYears(1);
            default:
                return LocalDateTime.now().minusDays(30); // Default to current date if duration not recognized
        }
    }


}
