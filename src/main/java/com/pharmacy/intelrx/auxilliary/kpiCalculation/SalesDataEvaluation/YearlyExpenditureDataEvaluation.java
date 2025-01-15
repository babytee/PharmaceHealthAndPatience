package com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation;

import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class YearlyExpenditureDataEvaluation {
    private final OrderRepository orderRepository;
    private final Utility utility;

    public Double SalesDataEvaluation(List<Order> orders) {

        // Filter orders for the current and previous years
        List<Order> currentYearOrders = filterOrdersByYear(orders, Year.now().getValue());
        List<Order> previousYearOrders = filterOrdersByYear(orders, Year.now().getValue() - 1);

        // Calculate total sales for the current and previous years
        double currentYearSales = calculateTotalSales(currentYearOrders);
        double previousYearSales = calculateTotalSales(previousYearOrders);

        // Calculate YoY sales percentage
        double yoySalesPercentage = calculateYoYSalesPercentage(currentYearSales, previousYearSales);

        return yoySalesPercentage;
    }

    private List<Order> filterOrdersByYear(List<Order> orders, int year) {
        List<Order> filteredOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getOrderedDate().getYear() == year) {
                filteredOrders.add(order);
            }
        }
        return filteredOrders;
    }

    private double calculateTotalSales(List<Order> orders) {
        double totalSales = 0;
        for (Order order : orders) {
            totalSales += order.getTotalAmount();
        }
        return totalSales;
    }

    private double calculateYoYSalesPercentage(double currentYearSales, double previousYearSales) {
        if (previousYearSales == 0) {
            return 0; // Avoid division by zero
        }
        return ((currentYearSales - previousYearSales) / previousYearSales) * 100;
    }
}
