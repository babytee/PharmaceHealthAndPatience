package com.pharmacy.intelrx.auxilliary.kpiCalculation.RetailFinancial;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.pharmacy.repositories.ExpenditureRepository;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CalculateNetProfitMargin {
    private final GeneralCalculation generalCalculation;
    private final ExpenditureRepository expenditureRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    public double getNetProfitMargin(String intelRxId, String duration) {
        /**
         Formula for Net Profit Margin:
         Net Profit Margin (%) = (Net Profit/Total Revenue)×100

         Where:

         Net Profit: The total revenue minus all expenses and taxes.
         Total Revenue: The sum of all sales revenue generated.
         Steps to Implement Net Profit Margin Calculation:
         Calculate Total Revenue:

         Use your existing OrderRepository to calculate the total sales revenue within a specified period.
         You may already have a method like getTotalRevenueForPeriod in your repository to get this value. If not,
         ensure you have a method that sums up the totalAmount from orders within a given period.

         */

        LocalDateTime startDate = generalCalculation.calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

        Double totalRevenue = orderRepository.getTotalRevenueForPeriod(startDate, endDate, intelRxId);
        Double totalCOGS = orderRepository.getTotalCOGS(startDate, endDate, intelRxId);

        // Check for null or zero values
        if (totalRevenue == null || totalCOGS == null || totalRevenue == 0 || totalCOGS == 0) {
            return 0.0; // Handle potential division by zero or null values
        }

        // Calculate net profit
        double netProfit = totalRevenue - totalCOGS;

        // Calculate net profit margin (%)
        return (netProfit / totalRevenue) * 100.0;
    }

}
