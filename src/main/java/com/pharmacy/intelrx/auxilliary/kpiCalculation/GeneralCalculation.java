package com.pharmacy.intelrx.auxilliary.kpiCalculation;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation.CalculateCostOfGoodsSold;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GeneralCalculation {

    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final CalculateCostOfGoodsSold calculateCOGS;

    public GeneralCalculation(InventoryRepository inventoryRepository, OrderRepository orderRepository, CalculateCostOfGoodsSold calculateCOGS) {
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
        this.calculateCOGS = calculateCOGS;
    }

    public double getAverageInventoryCost(String intelRxId, String duration) {

        LocalDateTime startDate = calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now(); // End date is the current date
        Double averageInventory = inventoryRepository.getAverageInventoryValue(startDate, endDate, intelRxId);
        // Check if averageInventory is null
        if (averageInventory == null) {
            return 0.0; // or handle it based on your business logic
        }

        return averageInventory;
    }

    public double calculateGrossProfit(String intelRxId, String duration) {

        // Determine start and end dates based on the provided duration
        LocalDateTime startDate = calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

        Double totalSales = orderRepository.getTotalSales(startDate, endDate, intelRxId);
        Double totalCOGS = calculateCOGS.calculateCOGS( intelRxId, duration);

        if (totalSales == null) {
            totalSales = 0.0;
        }
        if (totalCOGS == null) {
            totalCOGS = 0.0;
        }

        return totalSales - totalCOGS;
    }


    public LocalDateTime calculateStartDateBasedOnDuration(String duration) {
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
