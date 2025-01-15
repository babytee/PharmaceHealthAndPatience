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
public class CalculateCurrentRatio {
    private final GeneralCalculation generalCalculation;
    private final OrderRepository orderRepository;
    private final ExpenditureRepository expenditureRepository;
    private final InventoryRepository inventoryRepository;

    public double calculateCurrentRatio(String intelRxId, String duration) {
        /**
         Current Ratio= Current Assets/Current Liabilities

         In your case, we will assume:

         Current Assets: Includes cash and equivalents, accounts receivable, and inventory.
         Current Liabilities: Can be derived from the Expenditure entity.
         Steps to Implement Current Ratio Calculation

         Current Assets:
         Cash and Equivalents: Derived from Order (cashPayment, transferPayment, and posPayment).
         Accounts Receivable: Derived from Order (balance).
         Inventory: Derived from Inventory (costPrice * quantity).

         Current Liabilities:
         Derived from Expenditure (amountSpent).
         **/

        LocalDateTime startDate = generalCalculation.calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

        Double cashAndEquivalents = orderRepository.getTotalCashAndEquivalents(startDate, endDate, intelRxId);
        Double accountsReceivable = orderRepository.getTotalAccountsReceivable(startDate, endDate, intelRxId);
        Double inventoryValue = inventoryRepository.getTotalInventoryValue(startDate, endDate, intelRxId);
        Double currentLiabilities = expenditureRepository.getTotalExpenditures(startDate, endDate, intelRxId);

        if (cashAndEquivalents == null) {
            cashAndEquivalents = 0.0;
        }
        if (accountsReceivable == null) {
            accountsReceivable = 0.0;
        }
        if (inventoryValue == null) {
            inventoryValue = 0.0;
        }
        if (currentLiabilities == null) {
            currentLiabilities = 0.0;
        }

        double currentAssets = cashAndEquivalents + accountsReceivable + inventoryValue;

        if (currentLiabilities == 0) {
            return 0.0; // Avoid division by zero
        }

        return currentAssets / currentLiabilities;
    }

}
