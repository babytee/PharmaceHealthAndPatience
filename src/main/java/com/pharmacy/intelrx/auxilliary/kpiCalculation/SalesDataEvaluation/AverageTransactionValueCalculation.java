package com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation;


import com.pharmacy.intelrx.auxilliary.kpiCalculation.GeneralCalculation;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
@RequiredArgsConstructor
@Service
public class AverageTransactionValueCalculation {
    private final OrderRepository orderRepository;

    public double averageTransactionValue(String intelRxId, String duration) {
        // Determine start and end dates based on the provided duration
        LocalDateTime startDate = calculateStartDateBasedOnDuration(duration);
        LocalDateTime endDate = LocalDateTime.now();

        Double totalRevenue = orderRepository.getTotalRevenueForPeriod(startDate, endDate, intelRxId);
        Long numberOfTransactions = orderRepository.getNumberOfTransactionsForPeriod(startDate, endDate, intelRxId);

        if (totalRevenue == null) {
            totalRevenue = 0.0;
        }
        if (numberOfTransactions == null || numberOfTransactions == 0) {
            return 0.0;
        }

        return totalRevenue / numberOfTransactions;
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
