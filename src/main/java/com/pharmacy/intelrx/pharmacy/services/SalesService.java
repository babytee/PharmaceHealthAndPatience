package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.admin.dto.SalesStatsResponse;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.models.UserType;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.CartItemResponse;
import com.pharmacy.intelrx.pharmacy.dto.FilterSalesRequest;
import com.pharmacy.intelrx.pharmacy.dto.OrderResponse;
import com.pharmacy.intelrx.pharmacy.dto.ReceiptResponse;
import com.pharmacy.intelrx.pharmacy.dto.employee.JobInformationRequest;
import com.pharmacy.intelrx.pharmacy.models.*;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.JobTitle;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.models.employee.JobInformation;
import com.pharmacy.intelrx.pharmacy.repositories.*;
import com.pharmacy.intelrx.pharmacy.repositories.auxilliary.JobTitleRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.utility.SalesMapping;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("PharmacySalesService")
public class SalesService {
    private final UserDetailsService userDetailsService;
    private final OrderRepository orderRepository;
    private final SalesMapping salesMapping;
    private final CartItemRepository cartItemRepository;
    private final Utility utility;
    private final EmployeeRepository employeeRepository;
    private final OrderRefundRepository orderRefundRepository;
    private final CartItemRefundRepository cartItemRefundRepository;


    public ResponseEntity<?> getFilteredOrdersSalesHistory(FilterSalesRequest request, Pageable pageable) {
        try {
            var user = userDetailsService.getAuthenticatedUser();
            String intelRxId = userDetailsService.getIntelRxId();
            var branch = userDetailsService.getBranch();

            LocalDate startDate = null;
            LocalDate endDate = null;
            if (!utility.isNullOrEmpty(request.getStartDate()) && !utility.isNullOrEmpty(request.getEndDate())) {
                startDate = utility.convertStringToLocalDate(request.getStartDate());
                endDate = utility.convertStringToLocalDate(request.getEndDate());
            }
            Page<Order> orderPage = null;
            if (branch == null && user.getUserType() == UserType.OWNER) {
                orderPage = orderRepository.findByFilters(
                        request.getBranchId(),
                        startDate == null ? null : startDate.toString(),
                        endDate == null ? null : endDate.toString(),
                        request.getSalesPersonId(),
                        request.getCashierId(),
                        request.getSalesStatus(),
                        intelRxId,
                        true,
                        pageable);
            }

            if (user.getUserType() == UserType.EMPLOYEE) {
                Optional<Employee> optionalEmployee = employeeRepository.findByUserIdAndEmployeeIntelRxId(user.getId(), intelRxId);
                JobTitle jobTitle = null;
                if (optionalEmployee.isPresent()) {
                    Employee employee = optionalEmployee.get();
                    jobTitle = employee.getJobInformation().getJobTitle();
                    //return ResponseEntity.ok(StandardResponse.success(jobTitle));
                }


                if (branch != null) {
                    orderPage = orderRepository.findByFilters(
                            branch.getId(),
                            startDate == null ? null : startDate.toString(),
                            endDate == null ? null : endDate.toString(),
                            jobTitle.getId() == 3 ? user.getId() : request.getSalesPersonId(),
                            jobTitle.getId() == 2 ? user.getId() : request.getCashierId(),
                            request.getSalesStatus(),
                            intelRxId,
                            true,
                            pageable);

                } else {
                    orderPage = orderRepository.findByFilters(
                            request.getBranchId(),
                            startDate == null ? null : startDate.toString(),
                            endDate == null ? null : endDate.toString(),
                            jobTitle.getId() == 3 ? user.getId() : request.getSalesPersonId(),
                            jobTitle.getId() == 2 ? user.getId() : request.getCashierId(),
                            request.getSalesStatus(),
                            intelRxId,
                            true,
                            pageable);
                }
            }


            // Check if orderPage is not null before using it
            if (orderPage == null || orderPage.isEmpty()) {
                // Return user type if orderPage is not set or empty
                return ResponseEntity.ok(StandardResponse.success(orderPage));
            }

//             Proceed with mapping and processing orders
            Page<OrderResponse> responsePage;
            if (request.getDateFilter() == null || request.getDateFilter().isEmpty()) {
                // If date filter is null or empty, return the default response
                responsePage = orderPage.map(cartItem -> salesMapping.mapToOrderResponse(cartItem));
            } else {
                responsePage = orderPage.map(cartItem -> salesMapping.mapToOrderResponse(cartItem))
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
                        }).stream().collect(Collectors.collectingAndThen(Collectors.toList(), PageImpl::new));

            }

            return ResponseEntity.ok(StandardResponse.success(responsePage));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(StandardResponse.error(e.getMessage() + " nice"));
        }

    }

    public ResponseEntity<?> getSingleSalesOrder(Long orderId) {

        var user = userDetailsService.getAuthenticatedUser();
        String intelRxId = userDetailsService.getIntelRxId();

        if (orderId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("orderId is required"));
        }

        Optional<Order> orderOptional = orderRepository.findByIdAndIntelRxId(orderId, intelRxId);

        if (orderOptional.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(orderOptional.get()));
        }

        Order order = orderOptional.get();

        OrderResponse responsePage = salesMapping.mapToOrderResponse(order);

        return ResponseEntity.ok(StandardResponse.success(responsePage));
    }

    public ResponseEntity<?> salesHistoryDetails(Long orderId, Pageable pageable) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("User not authenticated"));
        }
        PharmacyBranch branch = userDetailsService.getBranch();
        String intelRxId = userDetailsService.getIntelRxId();
        JobInformationRequest jobReq = userDetailsService.getJobInfo();
        Long jobInfoId = (jobReq != null) ? jobReq.getJobTitleId() : null;
        if (intelRxId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId not found"));
        }

        Optional<Order> optional = null;
        if (user.getUserType() == UserType.OWNER) {
            optional = orderRepository.findByIdAndIntelRxId(orderId, intelRxId);
        } else {
            if (jobInfoId != null && jobInfoId.equals(2L)) {
                // Cashier
                optional = orderRepository.findByIdAndCashier(
                        orderId, intelRxId, branch == null ? null : branch.getId(), user);
            } else if (jobInfoId != null && jobInfoId.equals(3L)) {
                // Sales Person
                optional = orderRepository.findByIdAndSalesPerson(
                        orderId, intelRxId, branch == null ? null : branch.getId(), user);
            } else {
                optional = orderRepository.findByIdAndIntelRxId(orderId, intelRxId);
            }
        }

        if (!optional.isPresent()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("orderId is not found"));
        }

        Order order = optional.get();
        Page<CartItem> cartItems;

        if (user.getUserType() == UserType.OWNER) {
            cartItems = cartItemRepository.findByOrderAndIntelRxIdAndStatus(order, intelRxId, true, pageable);
        } else {
            // If additional criteria is needed for non-owners, adjust the repository method accordingly


            if (jobInfoId != null && (jobInfoId.equals(2L) || jobInfoId.equals(3L))) {
                // Cashier or Sales Person
                cartItems = cartItemRepository.findByOrderAndIntelRxIdAndStatusAndPharmacyBranchAndUser(
                        order, intelRxId, branch, true, user, pageable);
            } else {
                cartItems = cartItemRepository.findByOrderAndIntelRxIdAndStatus(order, intelRxId, true, pageable);
            }

            if (cartItems.isEmpty()) {
                return ResponseEntity.ok(StandardResponse.success(cartItems));
            }
        }

        if (cartItems.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(cartItems));
        }

        Page<CartItemResponse> responseList = cartItems.map(salesMapping::mapToCartItem);

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
        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();
        Optional<Order> orderOptional = null;
        if (branch == null && user.getUserType() == UserType.OWNER) {
            orderOptional = orderRepository.findByIdAndIntelRxId(
                    orderId, intelRxId
            );
        }
        if (branch == null && user.getUserType() == UserType.EMPLOYEE) {
            orderOptional = orderRepository.findByIdAndIntelRxId(
                    orderId, intelRxId
            );
        }
        if (branch != null && user.getUserType() == UserType.EMPLOYEE) {
            orderOptional = orderRepository.findByIdAndIntelRxIdAndPharmacyBranch(
                    orderId, intelRxId, branch
            );
        }
//
        if (orderOptional.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(orderOptional));
        }
//
        Order order = orderOptional.get();

        ReceiptResponse responsePage = salesMapping.mapToReceiptResponse(order);

        return ResponseEntity.ok(StandardResponse.success(responsePage));

    }

    public ResponseEntity<?> salesStats(FilterSalesRequest request) {
        SalesStatsResponse statsResponse = new SalesStatsResponse();
        String intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();
        var user = userDetailsService.getAuthenticatedUser();


        List<Order> orders = null;
        List<Order> completedOrders = null;
        List<OrderRefund> refunds = null;
        List<CartItemRefund> itemRefunds = null;

        if (branch == null && user.getUserType() == UserType.OWNER) {
            orders = orderRepository.findAllByIntelRxId(intelRxId);
            completedOrders = orderRepository.findAllByIntelRxIdAndSalesStatus(intelRxId, "Completed");
            refunds = orderRefundRepository.findAllByIntelRxId(intelRxId);
            itemRefunds = cartItemRefundRepository.findAllByIntelRxId(intelRxId);
        }

        Optional<Employee> optionalEmployee = employeeRepository.findByUserIdAndEmployeeIntelRxId(user.getId(), intelRxId);
        JobTitle jobTitle = null;

        if (user.getUserType() == UserType.EMPLOYEE) {
            if (optionalEmployee.isPresent()) {
                Employee employee = optionalEmployee.get();
                jobTitle = employee.getJobInformation().getJobTitle();
            }
            if (branch == null) {
                orders = orderRepository.findByFiltersForSalesStart(
                        request.getBranchId(),
                        null,
                        null,
                        jobTitle.getId() == 3 ? user.getId() : null,
                        jobTitle.getId() == 2 ? user.getId() : null,
                        null,
                        intelRxId,
                        true);

                completedOrders = orderRepository.findByFiltersForSalesStart(
                        request.getBranchId(),
                        null,
                        null,
                        jobTitle.getId() == 3 ? user.getId() : null,
                        jobTitle.getId() == 2 ? user.getId() : null,
                        "Completed",
                        intelRxId,
                        true);
                refunds = orderRefundRepository.findAllByIntelRxIdAndRefundedBy(intelRxId, user);
                itemRefunds = cartItemRefundRepository.findAllByIntelRxIdAndRefundedBy(intelRxId, user);
            } else {
                orders = orderRepository.findByFiltersForSalesStart(
                        branch.getId(),
                        null,
                        null,
                        jobTitle.getId() == 3 ? user.getId() : null,
                        jobTitle.getId() == 2 ? user.getId() : null,
                        null,
                        intelRxId,
                        true);

                completedOrders = orderRepository.findByFiltersForSalesStart(
                        branch.getId(),
                        null,
                        null,
                        jobTitle.getId() == 3 ? user.getId() : null,
                        jobTitle.getId() == 2 ? user.getId() : null,
                        "Completed",
                        intelRxId,
                        true);
                refunds = orderRefundRepository.findAllByIntelRxIdAndRefundedBy(intelRxId, user);
                itemRefunds = cartItemRefundRepository.findAllByIntelRxIdAndRefundedBy(intelRxId, user);
            }

        }

        Optional<String> dateFilterOptional = Optional.ofNullable(request.getDateFilter());

        double totalSales = orders.stream()
                .filter(order -> {
                    LocalDateTime orderedDate = order.getOrderedDate();
                    LocalDateTime now = LocalDateTime.now();
                    if (dateFilterOptional.isPresent()) {
                        switch (dateFilterOptional.get()) {
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
                                // Handle unknown filter case
                                throw new IllegalArgumentException("Unknown date filter: " + dateFilterOptional.get());
                        }
                    } else {
                        return true; // Include all orders when date filter is null
                    }
                })
                .mapToDouble(Order::getTotalAmount) // Map each order to its total amount
                .sum(); // Calculate the sum of total amounts


        double salesProfit = completedOrders.stream()
                .filter(order -> {
                    LocalDateTime orderedDate = order.getOrderedDate();
                    LocalDateTime now = LocalDateTime.now();
                    if (dateFilterOptional.isPresent()) {
                        switch (dateFilterOptional.get()) {
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
                                throw new IllegalArgumentException("Unknown date filter: " + dateFilterOptional.get());
                        }
                    } else {
                        return true; // Include all orders when date filter is null
                    }
                })
                .mapToDouble(order -> order.getCartItem().stream()
                        .mapToDouble(CartItem::calculateSalesProfitAmount)
                        .sum())
                .sum();


        double totalRefundAmount = refunds.stream()
                .filter(refund -> {
                    LocalDateTime refundedDate = refund.getRefundDate();
                    LocalDateTime now = LocalDateTime.now();
                    if (dateFilterOptional.isPresent()) {
                        switch (dateFilterOptional.get()) {
                            case "Today":
                                return refundedDate.toLocalDate().isEqual(now.toLocalDate());
                            case "Last 30 Days":
                                return refundedDate.isAfter(now.minusDays(30));
                            case "Last 3 Months":
                                return refundedDate.isAfter(now.minusMonths(3));
                            case "Last 6 Months":
                                return refundedDate.isAfter(now.minusMonths(6));
                            case "Last 1 Year":
                                return refundedDate.isAfter(now.minusYears(1));
                            default:
                                // Handle unknown filter case
                                throw new IllegalArgumentException("Unknown date filter: " + dateFilterOptional.get());
                        }
                    } else {
                        return true; // Include all orders when date filter is null
                    }
                })
                .mapToDouble(OrderRefund::getRefundAmount) // Map each refund to its total amount Refunded
                .sum(); // Calculate the sum of total amounts of RefundAmount

        double totalRefundItemAmount = itemRefunds.stream()
                .filter(refund -> {
                    LocalDateTime refundedDate = refund.getRefundDate();
                    LocalDateTime now = LocalDateTime.now();
                    if (dateFilterOptional.isPresent()) {
                        switch (dateFilterOptional.get()) {
                            case "Today":
                                return refundedDate.toLocalDate().isEqual(now.toLocalDate());
                            case "Last 30 Days":
                                return refundedDate.isAfter(now.minusDays(30));
                            case "Last 3 Months":
                                return refundedDate.isAfter(now.minusMonths(3));
                            case "Last 6 Months":
                                return refundedDate.isAfter(now.minusMonths(6));
                            case "Last 1 Year":
                                return refundedDate.isAfter(now.minusYears(1));
                            default:
                                // Handle unknown filter case
                                throw new IllegalArgumentException("Unknown date filter: " + dateFilterOptional.get());
                        }
                    } else {
                        return true; // Include all orders when date filter is null
                    }
                })
                .mapToDouble(CartItemRefund::getRefundAmount) // Map each refund to its total amount Refunded
                .sum(); // Calculate the sum of total amounts of RefundAmount

        double totalRefund = totalRefundAmount + totalRefundItemAmount;

        statsResponse.setRefunds(utility.roundAmount(totalRefund));
        statsResponse.setTotalSales(totalSales);
        statsResponse.setSalesProfit(salesProfit);

        return ResponseEntity.ok(StandardResponse.success(statsResponse));
    }

}
