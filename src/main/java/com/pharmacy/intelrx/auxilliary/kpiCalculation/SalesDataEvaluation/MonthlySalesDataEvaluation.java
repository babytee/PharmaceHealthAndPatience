package com.pharmacy.intelrx.auxilliary.kpiCalculation.SalesDataEvaluation;

import com.pharmacy.intelrx.pharmacy.models.CartItem;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MonthlySalesDataEvaluation {
    private final OrderRepository orderRepository;

    public String MomSalesDataEvaluation( List<Order> orders) {

        // Filter orders for the current and previous months
        List<Order> currentMonthOrders = filterOrdersByMonth(orders, YearMonth.now());
        List<Order> previousMonthOrders = filterOrdersByMonth(orders, YearMonth.now().minusMonths(1));

        // Calculate total sales for the current and previous months
        double currentMonthSales = calculateTotalSales(currentMonthOrders);
        double previousMonthSales = calculateTotalSales(previousMonthOrders);

        // Calculate MoM sales difference
        double momSalesDifference = currentMonthSales - previousMonthSales;

        // Determine if current month sales are higher, lower, or equal to previous month
        String comparisonResult;
        if (momSalesDifference > 0) {
            comparisonResult = "higher than";
        } else if (momSalesDifference < 0) {
            comparisonResult = "lower than";
        } else {
            comparisonResult = "equal to";
        }
        String msg = "Current month sales are " + comparisonResult + " previous month by " + Math.abs(momSalesDifference);

        return msg;
     }

    public List<Order> filterOrdersByMonth(List<Order> orders, YearMonth yearMonth) {
        List<Order> filteredOrders = new ArrayList<>();
        for (Order order : orders) {
            LocalDate orderDate = order.getOrderedDate().toLocalDate();
            YearMonth orderYearMonth = YearMonth.of(orderDate.getYear(), orderDate.getMonth());
            if (orderYearMonth.equals(yearMonth)) {
                filteredOrders.add(order);
            }
        }
        return filteredOrders;
    }

    public double calculateTotalSales(List<Order> orders) {
        double totalSales = 0;
        for (Order order : orders) {
            totalSales += order.getTotalAmount();
        }
        return totalSales;
    }


    public List<CartItem> filterCartsByMonth(List<CartItem> cartItems, YearMonth yearMonth) {
        List<CartItem> filteredCartItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            //if(cartItem.getInventory().equals(inventoryType)) {
                LocalDate cartDate = cartItem.getCreatedAt().toLocalDate();
                YearMonth cartYearMonth = YearMonth.of(cartDate.getYear(), cartDate.getMonth());
                if (cartYearMonth.equals(yearMonth)) {
                    filteredCartItems.add(cartItem);
                }
            //}
        }
        return filteredCartItems;
    }

    public LocalDateTime parseDateTime(String dateTimeString) {
        String[] components = dateTimeString.split("-");
        int month = Integer.parseInt(components[0]);
        int day = Integer.parseInt(components[1]);
        int year = Integer.parseInt(components[2]);
        return LocalDateTime.of(year, month, day, 0, 0);
    }

    public List<CartItem> filterCartsByDateRangeAndMonth(List<CartItem> cartItems, String startDateStr, String endDateStr, int targetYearMonth, String inventoryType) {
        List<CartItem> filteredCartItems = new ArrayList<>();
        LocalDateTime startDate = parseDateTime(startDateStr);
        LocalDateTime endDate = parseDateTime(endDateStr);

        // Extract target year and month
        int targetYear = targetYearMonth / 100;
        int targetMonth = targetYearMonth % 100;

        for (CartItem cartItem : cartItems) {
            if (cartItem.getInventory().equals(inventoryType)) {
                LocalDateTime cartDate = cartItem.getCreatedAt();

                // Extract year and month from cart item's date
                int cartYear = cartDate.getYear();
                int cartMonth = cartDate.getMonthValue();

                // Check if the cart item's year and month match the target year and month
                // Also check if it falls within the specified date range
                if (cartYear == targetYear && cartMonth == targetMonth &&
                        !cartDate.isBefore(startDate) && !cartDate.isAfter(endDate)) {
                    filteredCartItems.add(cartItem);
                }
            }
        }
        return filteredCartItems;
    }


    public List<CartItem> filterCartsByDateRangeWeekly(List<CartItem> cartItems, LocalDateTime startDate, LocalDateTime endDate, int targetYearMonth, String inventoryType) {
        List<CartItem> filteredCartItems = new ArrayList<>();

        // Extract target year and month
        int targetYear = targetYearMonth / 100;
        int targetMonth = targetYearMonth % 100;

        for (CartItem cartItem : cartItems) {
            if (cartItem.getInventory().getInventoryType().equals(inventoryType)) {
                LocalDateTime cartDate = cartItem.getCreatedAt();

                // Extract year and month from cart item's date
                int cartYear = cartDate.getYear();
                int cartMonth = cartDate.getMonthValue();

                // Check if the cart item's year and month match the target year and month
                // Also check if it falls within the specified date range
                if (cartYear == targetYear && cartMonth == targetMonth &&
                        !cartDate.isBefore(startDate) && !cartDate.isAfter(endDate)) {
                    filteredCartItems.add(cartItem);
                }
            }
        }
        return filteredCartItems;
    }


    public double calculateTotalCartSales(List<CartItem> cartItems) {
        double totalSales = 0;
        for (CartItem cartItem : cartItems) {
            totalSales += cartItem.getAmount();
        }
        return totalSales;
    }
}

