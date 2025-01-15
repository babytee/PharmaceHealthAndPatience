package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.auxilliary.models.PaymentMethod;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.ExpenditureRequest;
import com.pharmacy.intelrx.pharmacy.dto.OrderRefundRequest;
import com.pharmacy.intelrx.pharmacy.dto.OrderRefundResponse;
import com.pharmacy.intelrx.pharmacy.models.CartItem;
import com.pharmacy.intelrx.pharmacy.models.CartItemRefund;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.models.OrderRefund;
import com.pharmacy.intelrx.pharmacy.repositories.CartItemRefundRepository;
import com.pharmacy.intelrx.pharmacy.repositories.CartItemRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRefundRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import com.pharmacy.intelrx.pharmacy.utility.OrderRefundValidation;
import com.pharmacy.intelrx.pharmacy.utility.PharmacyMapping;
import com.pharmacy.intelrx.utility.Auxiliary;
import com.pharmacy.intelrx.utility.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderRefundService {
    private final OrderRefundRepository refundRepository;
    private final OrderRefundValidation refundValidation;
    private final UserDetailsService userDetailsService;
    private final Auxiliary auxiliary;
    private final OrderRepository orderRepository;
    private final PharmacyMapping pharmacyMapping;
    private final ExpenditureService expenditureService;
    private final CartItemRefundRepository cartItemRefundRepository;
    private final CartItemRepository cartItemRepository;


    public ResponseEntity<?> issueRefund(OrderRefundRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("You Are Un-Authorize"));
        }

        ResponseEntity responseEntity = refundValidation.validateRefundRequest(request, "Order");

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        AddOrderRefund(request);

        return ResponseEntity.ok(StandardResponse.success("Refunded Submitted Successfully"));
    }

    public void AddOrderRefund(OrderRefundRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        String intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        PaymentMethod paymentMethod = auxiliary.getPaymentMethod(request.getPaymentMethodId());

        Optional<Order> orderOptional = orderRepository.findById(request.getOrderId());
        Order order = orderOptional.get();

        OrderRefund orderRefund = OrderRefund.builder()
                .refundAmount(request.getRefundAmount())
                .refundMethod(paymentMethod)
                .intelRxId(intelRxId)
                .pharmacyBranch(branch != null ? branch : null)
                .refundedBy(user)
                .reasonForRefund(request.getReasonForRefund())
                .optionalReason(request.getOptionalReason())
                .refundDate(LocalDateTime.now())
                .order(order).build();

        refundRepository.save(orderRefund);


        order.setSalesStatus("Refunded");
        orderRepository.save(order);

        //Submit Expenses
//        ExpenditureRequest expenditureRequest = new ExpenditureRequest();
//        expenditureRequest.setExpenseName("Order Refund By " + user.getFirstName());
//        expenditureRequest.setExpDay(LocalDateTime.now().getDayOfMonth());
//        expenditureRequest.setExpMonth(LocalDateTime.now().getMonthValue());
//        expenditureRequest.setExpYear(LocalDateTime.now().getYear());
//        expenditureRequest.setAddedBy(user);
//        expenditureRequest.setAmountSpent(request.getRefundAmount());
//        expenditureService.modifyExpenseEntity(expenditureRequest, null);


    }

    public ResponseEntity<?> issueItemRefund(List<OrderRefundRequest> requests) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("You Are Un-Authorize"));
        }

        for (OrderRefundRequest request : requests) {

            ResponseEntity responseEntity = refundValidation.validateRefundRequest(request, "Cart");

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                return responseEntity;
            }
            AddItemRefund(request);
        }

        return ResponseEntity.ok(StandardResponse.success("Refunded Submitted Successfully"));
    }

    public void AddItemRefund(OrderRefundRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        String intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        PaymentMethod paymentMethod = auxiliary.getPaymentMethod(request.getPaymentMethodId());

        Optional<CartItem> orderOptional = cartItemRepository.findById(request.getCartItemId());
        CartItem cartItem = orderOptional.get();

        CartItemRefund orderRefund = CartItemRefund.builder()
                .refundAmount(request.getRefundAmount())
                .refundMethod(paymentMethod)
                .intelRxId(intelRxId)
                .pharmacyBranch(branch != null ? branch : null)
                .refundedBy(user)
                .reasonForRefund(request.getReasonForRefund())
                .optionalReason(request.getOptionalReason())
                .refundDate(LocalDateTime.now())
                .cartItem(cartItem).build();

        cartItemRefundRepository.save(orderRefund);

        //Submit Expenses
//        ExpenditureRequest expenditureRequest = new ExpenditureRequest();
//        expenditureRequest.setExpenseName("Item Refunded By " + user.getFirstName());
//        expenditureRequest.setExpDay(LocalDateTime.now().getDayOfMonth());
//        expenditureRequest.setExpMonth(LocalDateTime.now().getMonthValue());
//        expenditureRequest.setExpYear(LocalDateTime.now().getYear());
//        expenditureRequest.setAddedBy(user);
//        expenditureRequest.setAmountSpent(request.getRefundAmount());
//        expenditureService.modifyExpenseEntity(expenditureRequest, null);


    }

    public ResponseEntity<?> getRefund(Long orderId) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("This user is un authorized"));
        }

        String intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        if (orderId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("orderId cannot be empty"));
        }
        List<OrderRefund> orderList = refundRepository.findByOrderId(orderId);
        List<OrderRefundResponse> responseList = orderList.stream().map(orderRefund -> mapToRefundResponse(orderRefund)).collect(Collectors.toList());

        if (orderList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(orderList));
        }
        return ResponseEntity.ok(StandardResponse.success(responseList));

    }

    public OrderRefundResponse mapToRefundResponse(OrderRefund orderRefund) {

        OrderRefundResponse orderRefundResponse = new OrderRefundResponse();
        orderRefundResponse.setId(orderRefund.getId());
        orderRefundResponse.setReasonForRefund(orderRefund.getOptionalReason());
        orderRefundResponse.setRefundAmount(orderRefund.getRefundAmount());
        orderRefundResponse.setReasonForRefund(orderRefund.getReasonForRefund());
        orderRefundResponse.setRefundDate(orderRefund.getRefundDate());
        orderRefundResponse.setRefundMethod(orderRefund.getRefundMethod());
        orderRefundResponse.setRefundedBy(userDetailsService.mapToUserResponse(orderRefund.getRefundedBy()));
        if (orderRefund.getPharmacyBranch() == null) {
            orderRefundResponse.setPharmacyBranch(null);
        } else {
            orderRefundResponse.setPharmacyBranch(pharmacyMapping.mapToBranchResponse(orderRefund.getPharmacyBranch()));
        }
        orderRefundResponse.setIntelRxId(orderRefund.getIntelRxId());
        orderRefundResponse.setRefundDate(orderRefund.getRefundDate());
        return orderRefundResponse;

    }


    public OrderRefundResponse mapToOrderRefund(Order order) {
        var branch = userDetailsService.getBranch();
        var intelRxId = userDetailsService.getIntelRxId();
        Optional<OrderRefund> optional = refundRepository.findByOrderIdAndPharmacyBranch(order.getId(), branch);
        OrderRefund orderRefund = null;
        if (optional.isPresent()) {
            orderRefund = optional.get();
            List<CartItem> cartItems = cartItemRepository.findByIntelRxIdAndAndOrderAndStatus
                    (intelRxId, order, true);
            for (CartItem cartItem : cartItems) {
                //submit to refund cart item
                OrderRefundRequest orderRefundRequest = OrderRefundRequest.builder()
                        .refundAmount(cartItem.getAmount())
                        .paymentMethodId(orderRefund.getRefundMethod().getId())
                        .reasonForRefund(orderRefund.getReasonForRefund())
                        .optionalReason(orderRefund.getOptionalReason())
                        .cartItemId(cartItem.getId()).build();
                AddItemRefund(orderRefundRequest);
            }
            OrderRefundResponse responseList = mapToRefundResponse(orderRefund);
            return responseList;
        }
        return null;

    }

    public OrderRefundResponse mapToItemRefundResponse(CartItemRefund orderRefund) {

        OrderRefundResponse orderRefundResponse = new OrderRefundResponse();
        orderRefundResponse.setId(orderRefund.getId());
        orderRefundResponse.setReasonForRefund(orderRefund.getOptionalReason());
        orderRefundResponse.setRefundAmount(orderRefund.getRefundAmount());
        orderRefundResponse.setReasonForRefund(orderRefund.getReasonForRefund());
        orderRefundResponse.setRefundDate(orderRefund.getRefundDate());
        orderRefundResponse.setRefundMethod(orderRefund.getRefundMethod());
        orderRefundResponse.setRefundedBy(userDetailsService.mapToUserResponse(orderRefund.getRefundedBy()));
        if (orderRefund.getPharmacyBranch() == null) {
            orderRefundResponse.setPharmacyBranch(null);
        } else {
            orderRefundResponse.setPharmacyBranch(pharmacyMapping.mapToBranchResponse(orderRefund.getPharmacyBranch()));
        }
        orderRefundResponse.setIntelRxId(orderRefund.getIntelRxId());
        orderRefundResponse.setRefundDate(orderRefund.getRefundDate());
        return orderRefundResponse;

    }

    public OrderRefundResponse mapToItemRefund(Long cartItemId) {
        var branch = userDetailsService.getBranch();
        Optional<CartItemRefund> itemRefundOptional = cartItemRefundRepository.findByCartItemIdAndPharmacyBranch(cartItemId, branch);
        if (itemRefundOptional.isPresent()) {
            CartItemRefund cartItemRefund = itemRefundOptional.get();
            OrderRefundResponse orderRefundResponse = mapToItemRefundResponse(cartItemRefund);
            return orderRefundResponse;
        }
        return null;

    }


}
