package com.pharmacy.intelrx.auxilliary.kpiCalculation.RetailFinancial;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.pharmacy.repositories.ExpenditureRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CalculateQuickRatio {
    private final GeneralCalculation generalCalculation;
    private final OrderRepository orderRepository;
    private final ExpenditureRepository expenditureRepository;

    public double calculateQuickRatio(String intelRxId, String duration) {
        /**
         Quick Ratio= Cash and Cash Equivalents+Accounts Receivable/Current Liabilities

         Cash and Cash Equivalents: This could be derived from orders (e.g., total payments made in cash, transfer, and POS).
         Accounts Receivable: This could also be derived from orders that have balances (i.e., amounts that are yet to be paid).
         Current Liabilities: This could be derived from expenditures.
         **/

        LocalDateTime startDate = generalCalculation.calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

        Double cashAndEquivalents = orderRepository.getTotalCashAndEquivalents(startDate, endDate, intelRxId);
        Double accountsReceivable = orderRepository.getTotalAccountsReceivable(startDate, endDate, intelRxId);
        Double currentLiabilities = expenditureRepository.getTotalExpenditures(startDate, endDate, intelRxId);

        if (cashAndEquivalents == null) {
            cashAndEquivalents = 0.0;
        }
        if (accountsReceivable == null) {
            accountsReceivable = 0.0;
        }
        if (currentLiabilities == null) {
            currentLiabilities = 0.0;
        }

        if (currentLiabilities == 0) {
            return 0.0; // Avoid division by zero
        }

        return (cashAndEquivalents + accountsReceivable) / currentLiabilities;
    }

}
