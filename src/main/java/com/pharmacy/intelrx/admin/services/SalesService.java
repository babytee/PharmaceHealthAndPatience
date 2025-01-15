package com.pharmacy.intelrx.admin.services;

import com.pharmacy.intelrx.admin.dto.SalesStatsResponse;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.CartItemResponse;
import com.pharmacy.intelrx.pharmacy.dto.FilterSalesRequest;
import com.pharmacy.intelrx.pharmacy.dto.OrderResponse;
import com.pharmacy.intelrx.pharmacy.dto.ReceiptResponse;
import com.pharmacy.intelrx.pharmacy.models.*;
import com.pharmacy.intelrx.pharmacy.repositories.CartItemRefundRepository;
import com.pharmacy.intelrx.pharmacy.repositories.CartItemRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRefundRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import com.pharmacy.intelrx.pharmacy.utility.SalesMapping;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("AdminPharmacySalesService")
public class SalesService {
    private final UserDetailsService userDetailsService;
    private final OrderRepository orderRepository;
    private final SalesMapping salesMapping;
    private final CartItemRepository cartItemRepository;
    private final Utility utility;
    private final OrderRefundRepository orderRefundRepository;
    private final CartItemRefundRepository cartItemRefundRepository;


    public ResponseEntity<?> getFilteredOrdersSalesHistory(FilterSalesRequest request, Pageable pageable) {

        Long branchId = request.getBranchId() == null ? null : request.getBranchId();
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (!utility.isNullOrEmpty(request.getStartDate()) && !utility.isNullOrEmpty(request.getEndDate())) {
            startDate = utility.convertStringToLocalDate(request.getStartDate());
            endDate = utility.convertStringToLocalDate(request.getEndDate());
        }

        Page<Order> orderPage = orderRepository.findByFilters(
                branchId,
                startDate == null ? null : startDate.toString(),
                endDate == null ? null : endDate.toString(),
                request.getSalesPersonId(),
                request.getCashierId(),
                request.getSalesStatus(),
                request.getIntelRxId(),
                true,
                pageable);

        if (orderPage.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(orderPage));
        }

        Page<OrderResponse> responsePage;
        if (request.getDateFilter() == null || request.getDateFilter().isEmpty()) {
            // If date filter is null or empty, return the default response
            responsePage = orderPage.map(cartItem -> salesMapping.mapToOrderResponse(cartItem));
        } else {
            responsePage = orderPage.stream().map(cartItem -> salesMapping.mapToOrderResponse(cartItem))
                    .filter(orderResponse -> {
                        LocalDateTime orderedDate = orderResponse.getOrderedDate();
                        LocalDateTime now = LocalDateTime.now();

                        switch (request.getDateFilter()) {
                            case "Today":
                                return orderedDate.toLocalDate().isEqual(now.toLocalDate());
                            case "Last 30 Days":
                                return orderedDate.isAfter(now.minusDays(30));
                            case "Last 3 Months":
                                return orderedDate.isAfter(now.minusMonths(3));
                            case "Last 6 Months":
                                return orderedDate.isAfter(now.minusMonths(6));
                            case "Last 1 Year":
                                return orderedDate.isAfter(now.minusYears(1));
                            default:
                                // Handle default case or throw an exception
                                return true;
                        }
                    }).collect(Collectors.collectingAndThen(Collectors.toList(), PageImpl::new));
            ;
        }

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    public ResponseEntity<?> getSingleSalesOrder(Long orderId) {

        if (orderId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("orderId is required"));
        }

        Optional<Order> orderOptional = orderRepository.findById(orderId);

        if (orderOptional.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(orderOptional.get()));
        }

        Order order = orderOptional.get();

        OrderResponse responsePage = salesMapping.mapToOrderResponse(order);

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    public ResponseEntity<?> salesHistoryDetails(Long orderId, String intelRxId, Pageable pageable) {

        Optional<Order> optional = orderRepository.findById(orderId);
        if (!optional.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("orderId is not found"));
        }
        Order order = optional.get();

        Page<CartItem> orderList = cartItemRepository.findByOrderAndIntelRxIdAndStatus(order, intelRxId, true, pageable);
        if (orderList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(orderList));
        }
        Page<CartItemResponse> responseList = orderList.map((cartItem) -> salesMapping.mapToCartItem(cartItem));

        return ResponseEntity.ok(StandardResponse.success(responseList));

    }

    public ResponseEntity<?> viewReceipt(Long orderId) {

        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Un authorized"));
        }
        if (orderId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("orderId is required"));
        }

        Optional<Order> orderOptional = orderRepository.findById(orderId);

        if (orderOptional.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(orderOptional.get()));
        }

        Order order = orderOptional.get();

        ReceiptResponse responsePage = salesMapping.mapToReceiptResponse(order);

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    public ResponseEntity<?> salesStats(String intelRxId) {
        SalesStatsResponse statsResponse = new SalesStatsResponse();

        List<CartItemRefund> itemRefunds = null;
        List<CartItem> cartItems = null;
        var orders = orderRepository.findAllByIntelRxId(intelRxId);

        // Initialize total orders amount to zero
        double totalSales = 0;
        double salesProfit = 0.0;
        // Sum up the order amounts
        for (Order order : orders) {
            totalSales += order.getTotalAmount();

            cartItems = cartItemRepository.findByIntelRxIdAndAndOrder(intelRxId, order);
            for (CartItem cartItem : cartItems) {
                salesProfit += cartItem.calculateSalesProfitAmount();
            }

        }

//        var completedOrders = orderRepository.findAllByIntelRxIdAndSalesStatus(intelRxId, "Completed");
//        // Initialize total orders amount to zero
//        double completedSales = 0;
//        // Sum up the order amounts
//        for (Order order : completedOrders) {
//            completedSales += order.getTotalAmount();
//        }

        var refunds = orderRefundRepository.findAllByIntelRxId(intelRxId);
        // Initialize total refund amount to zero
        double totalRefundAmount = 0.0;
        // Sum up the refund amounts
        for (OrderRefund refund : refunds) {
            totalRefundAmount += refund.getRefundAmount();
        }


        itemRefunds = cartItemRefundRepository.findAllByIntelRxId(intelRxId);

        // Initialize total refund amount to zero
        // Sum up the refund amounts
        for (CartItemRefund refund : itemRefunds) {
            totalRefundAmount += refund.getRefundAmount();
        }

        statsResponse.setRefunds(totalRefundAmount);
        statsResponse.setTotalSales(totalSales);
        statsResponse.setSalesProfit(salesProfit);

        return ResponseEntity.ok(StandardResponse.success(statsResponse));
    }


}


