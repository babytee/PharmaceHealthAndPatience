package com.pharmacy.intelrx.auxilliary.kpiCalculation.RetailFinancial;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.pharmacy.repositories.ExpenditureRepository;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CalculateAccountsPayableTurnover {
    private final GeneralCalculation generalCalculation;
    private final ExpenditureRepository expenditureRepository;
    private final InventoryRepository inventoryRepository;

    public double calculateAccountsPayableTurnover(String intelRxId, String duration) {
        /**
         Accounts Payable Turnover= Total Purchases/Average Accounts Payable

         In your case, we'll assume:

         Total Purchases: This can be derived from the Inventory entity by summing up the costPrice of the items purchased within a given period.
         Average Accounts Payable: This can be derived from the Expenditure entity, specifically expenditures that are liabilities (like supplier payments).

         Steps to Implement Accounts Payable Turnover Calculation
         Total Purchases: Sum of costPrice from Inventory.
         Average Accounts Payable: Average of amountSpent from Expenditure over a given period.

         */

        LocalDateTime startDate = generalCalculation.calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

        Double totalPurchases = inventoryRepository.getTotalInventoryValue(startDate, endDate, intelRxId);
        Double averageAccountsPayable = expenditureRepository.getAverageAccountsPayable(startDate, endDate, intelRxId,"Supplier");

        if (totalPurchases == null) {
            totalPurchases = 0.0;
        }
        if (averageAccountsPayable == null) {
            averageAccountsPayable = 0.0;
        }

        if (averageAccountsPayable == 0) {
            return 0.0; // Avoid division by zero
        }

        return totalPurchases / averageAccountsPayable;
    }

}
