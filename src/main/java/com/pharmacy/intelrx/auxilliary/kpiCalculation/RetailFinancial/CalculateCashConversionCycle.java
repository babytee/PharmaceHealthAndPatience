package com.pharmacy.intelrx.auxilliary.kpiCalculation.RetailFinancial;

import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation.CalculateCostOfGoodsSold;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
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
public class CalculateCashConversionCycle {
    private final GeneralCalculation generalCalculation;
    private final ExpenditureRepository expenditureRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    public double getCalculateCashConversionCycle(String intelRxId, String duration) {
        /**

         Components of Cash Conversion Cycle (CCC):
         Inventory Conversion Period (ICP):

         Measures the average time taken to sell inventory and convert it into sales.
         Formula:
         ICP = Average Inventory/Cost of Goods Sold (COGS) per day

             Average Inventory: Typically calculated as
             Beginning Inventory+Ending Inventory/2

             COGS per day: Calculated as
             COGS/Number of days in the period
         ________________________________

         Accounts Receivable Period (ARP):

         Indicates the average time it takes for the company to collect its accounts receivable (credit sales).
         Formula:
         ARP = Accounts Receivable/Credit Sales per day

             Accounts Receivable: Total amount owed by customers for credit sales.
             Credit Sales per day: Calculated as
             Total Credit Sales/Number of days in the period
         ________________________________

         Accounts Payable Period (APP):

         Measures the average time it takes for the company to pay its suppliers.
         Formula:
         APP = Accounts Payable/Purchases per day

             Accounts Payable: Total amount owed to suppliers for purchases.
             Purchases per day: Calculated as
             Total Purchases/Number of days in the period
         ________________________________

         Calculation of Cash Conversion Cycle (CCC):
         Once you have calculated the Inventory Conversion Period (ICP), Accounts Receivable Period (ARP), and Accounts Payable Period (APP), you can compute the Cash Conversion Cycle using the formula:

         CCC = ICP + ARP − APP
       
         */

        LocalDateTime startDate = generalCalculation.calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now(); // End date is the current date

        // Calculate Inventory Conversion Period (ICP)
        Double totalCOGS = orderRepository.getTotalCOGS(startDate, endDate, intelRxId);
        Integer unitsSold = orderRepository.getUnitsSold(startDate, endDate, intelRxId);
        double COGSPerDay = totalCOGS != null && Duration.between(startDate, endDate).toDays() != 0 ? totalCOGS / Duration.between(startDate, endDate).toDays() : 0.0;
        double ICP = unitsSold != null && COGSPerDay != 0 ? (double) unitsSold / COGSPerDay : 0.0;

        // Calculate Accounts Receivable Period (ARP)
        Double accountsReceivable = orderRepository.getTotalAccountsReceivable(startDate, endDate, intelRxId);
        double ARP = accountsReceivable != null && orderRepository.getNumberOfTransactionsForPeriod(startDate, endDate, intelRxId) != 0 ? accountsReceivable / (orderRepository.getNumberOfTransactionsForPeriod(startDate, endDate, intelRxId) / Duration.between(startDate, endDate).toDays()) : 0.0;

        // Calculate Accounts Payable Period (APP)
        Double accountsPayable = expenditureRepository.getTotalAccountsPayable(startDate, endDate, intelRxId, "Supplier");
        double purchasesPerDay = inventoryRepository.getPurchasesValueDuringPeriod(startDate, endDate, intelRxId) != null && Duration.between(startDate, endDate).toDays() != 0 ? inventoryRepository.getPurchasesValueDuringPeriod(startDate, endDate, intelRxId) / Duration.between(startDate, endDate).toDays() : 0.0;
        double APP = accountsPayable != null && purchasesPerDay != 0 ? accountsPayable / purchasesPerDay : 0.0;

        // Calculate Cash Conversion Cycle (CCC)
        return ICP + ARP - APP;
    }

}
