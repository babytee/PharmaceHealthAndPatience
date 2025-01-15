package com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class YearlySalesDataEvaluation {
    private final OrderRepository orderRepository;
    private final Utility utility;

    public double salesDataEvaluation(String intelRxId) {
        //Year Over Year Sales Calculation
        //The most common way to track sales is by comparing quarterly sales with those of the previous year.
        //This KPI is calculated using the formular
        //Year -Over - Year Sales = ((Current Year’s Sales –Last Year’s Sales))/Last Year’s Sales) *100 %

        Double salesCurrentYear = orderRepository.getTotalSalesForYear(Year.now().getValue(), intelRxId);
        Double salesPreviousYear = orderRepository.getTotalSalesForYear(Year.now().getValue() - 1, intelRxId);

        if (salesCurrentYear == null) {
            salesCurrentYear = 0.0;
        }
        if (salesPreviousYear == null) {
            salesPreviousYear = 0.0;
        }

        if (salesPreviousYear == 0) {
            return 0.0; // Return 0 or any other appropriate value to indicate no growth
        }

        // Calculate YoY sales percentage
        return ((salesCurrentYear - salesPreviousYear) / salesPreviousYear) * 100;

    }

}
