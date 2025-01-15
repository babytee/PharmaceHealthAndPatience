package com.pharmacy.intelrx.auxilliary.kpiCalculation.Merchandising;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation.CalculateCostOfGoodsSold;
import com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation.CalculateSellThrough;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CalculateInventoryTurnover {

    private final CalculateCostOfGoodsSold calculateCostOfGoodsSold;
    private final GeneralCalculation generalCalculation;

    public double calculateInventoryTurnover(String intelRxId, String duration) {
        /**
         Formula for Inventory Turnover
         Inventory Turnover = Cost of Goods Sold (COGS)/Average Inventory
         **/

        // Calculate the total Cost of Goods Sold (COGS) over a specific period
        Double totalCOGS = calculateCostOfGoodsSold.calculateCOGS(intelRxId,duration);

        Double averageInventory = generalCalculation.getAverageInventoryCost(intelRxId,duration);

        if (averageInventory == null) {
            averageInventory = 0.0;
        }

        if (totalCOGS == null) {
            totalCOGS = 0.0;
        }

        if (averageInventory == 0) {
            return 0.0; // Avoid division by zero
        }

        return totalCOGS / averageInventory;
    }


}
