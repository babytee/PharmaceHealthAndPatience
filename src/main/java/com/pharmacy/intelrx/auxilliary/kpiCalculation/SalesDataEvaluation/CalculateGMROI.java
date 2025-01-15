package com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.pharmacy.models.Expenditure;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.repositories.ExpenditureRepository;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CalculateGMROI {
    private final GeneralCalculation generalCalculation;
    private final InventoryRepository inventoryRepository;

    public double calculateGMROI(String intelRxId, String duration) {
        //GMROI=Average Inventory Cost/Gross Profit

        LocalDateTime startDate = generalCalculation.calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

        // Fetch gross profit and average inventory cost
        double grossProfit = generalCalculation.calculateGrossProfit(intelRxId,duration);
        Double averageInventoryCost = inventoryRepository.getAverageInventoryValue(startDate,endDate,intelRxId);

        // Handle null case for averageInventoryCost
        if (averageInventoryCost == null) {
            averageInventoryCost = 0.0; // or handle differently based on your business logic
        }

        // Calculate GMROI
        double gmroi = 0.0;
        if (averageInventoryCost > 0 && grossProfit > 0) {
            gmroi = grossProfit / averageInventoryCost;
        }
        return gmroi;
    }

}
