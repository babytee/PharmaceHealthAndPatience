package com.pharmacy.intelrx.auxilliary.kpiCalculation.RetailFinancial;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation.CalculateCostOfGoodsSold;
import com.pharmacy.intelrx.pharmacy.repositories.ExpenditureRepository;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CalculateDPO {
    private final GeneralCalculation generalCalculation;
    private final ExpenditureRepository expenditureRepository;
    private final CalculateCostOfGoodsSold calculateCOGS;

    public double getCalculateDIO(String intelRxId, String duration) {
        /**
         * Days Payables Outstanding (DPO): Measures the average number of days it takes a company to pay its suppliers.
         * It is calculated as:
         DPO=Accounts Payable×Number of Days/Cost of Goods Sold (COGS)

         Accounts Payable: Total amount owed to suppliers.
         Number of Days: Typically 365 days (to annualize the metric) or a specific period.
         COGS: Cost of Goods Sold, which represents the direct costs attributable to the production of goods sold by a company.

         Adjustments Needed for Days Payables Outstanding (DPO)
         To calculate DPO using the provided method and repository, you would need to make several adjustments:

         Total Accounts Payable: Instead of deriving this from inventory-related data, you need to directly fetch the total accounts payable from your financial records or a dedicated repository that manages accounts payable.

         Cost of Goods Sold (COGS): Ensure you have a method in your repository (COGSRepository or similar) that can fetch the total COGS for the same period as startDate to endDate.
         */

        LocalDateTime startDate = generalCalculation.calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

        Double accountsPayable = expenditureRepository.getTotalAccountsPayable(startDate, endDate, intelRxId, "Supplier");
        Double totalCOGS = calculateCOGS.calculateCOGS(intelRxId, duration);

        // Calculate DPO
        if (totalCOGS == null || totalCOGS == 0) {
            return 0.0; // To avoid division by zero
        }

        // Assuming a default period of 365 days
        int numberOfDays = 365;
        return (accountsPayable * numberOfDays) / totalCOGS;
    }

}
