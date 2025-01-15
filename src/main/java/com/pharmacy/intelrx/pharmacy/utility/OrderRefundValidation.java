package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.auxilliary.models.PaymentMethod;
import com.pharmacy.intelrx.auxilliary.repositories.PaymentMethodRepository;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.OrderRefundRequest;
import com.pharmacy.intelrx.pharmacy.dto.PatientRequest;
import com.pharmacy.intelrx.pharmacy.models.CartItem;
import com.pharmacy.intelrx.pharmacy.models.CartItemRefund;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.models.OrderRefund;
import com.pharmacy.intelrx.pharmacy.repositories.CartItemRefundRepository;
import com.pharmacy.intelrx.pharmacy.repositories.CartItemRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRefundRepository;
import com.pharmacy.intelrx.pharmacy.repositories.OrderRepository;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderRefundValidation {
    private final Utility utility;
    private final OrderRepository orderRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final OrderRefundRepository orderRefundRepository;
    private final CartItemRefundRepository cartItemRefundRepository;
    private final CartItemRepository cartItemRepository;

    public ResponseEntity<?> validateRefundRequest(OrderRefundRequest request, String refundType) {
        if (utility.isNullOrEmpty(request.getReasonForRefund())) {
            return ResponseEntity.badRequest().body(StandardResponse.error("reasonForRefund is required"));
        }

        if (request.getRefundAmount() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("refundAmount is required"));
        }

        if (request.getPaymentMethodId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("paymentMethodId is required"));
        }

        if (refundType == "Cart") {
            if (request.getCartItemId() == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("cartItemId is required "));
            }

            Optional<CartItem> orderOptional = cartItemRepository.findById(request.getCartItemId());
            if (!orderOptional.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("item not found"));
            }

            List<CartItemRefund> optionalOrderRefund = cartItemRefundRepository.findByCartItemId(request.getCartItemId());
            double sumTotal = 0;
            if (!optionalOrderRefund.isEmpty()) {
                for (CartItemRefund orderRefund : optionalOrderRefund)
                    sumTotal += orderRefund.getRefundAmount();
            }

            CartItem cartItem = orderOptional.get();
            double total = cartItem.getAmount() - sumTotal;
            if (request.getRefundAmount() > total) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Your refund amount should be:" + total + " Naira"));
            }

            if (cartItem.getAmount() == sumTotal) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Your refund is completed"));
            }

            Optional<PaymentMethod> paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId());
            if (!paymentMethod.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("paymentMethod not found"));
            }
        }

        if (refundType == "Order") {
            if (request.getOrderId() == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("orderId is required "));
            }

            Optional<Order> orderOptional = orderRepository.findById(request.getOrderId());
            if (!orderOptional.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("order not found"));
            }

            List<OrderRefund> optionalOrderRefund = orderRefundRepository.findByOrderId(request.getOrderId());
            double sumTotal = 0;
            if (!optionalOrderRefund.isEmpty()) {
                for (OrderRefund orderRefund : optionalOrderRefund)
                    sumTotal += orderRefund.getRefundAmount();
            }

            Order order = orderOptional.get();
            double total = order.getTotalAmount() - sumTotal;
            if (request.getRefundAmount() > total) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Your refund amount should be:" + total + " Naira"));
            }

            if (order.getTotalAmount() == sumTotal) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Your refund is completed"));
            }

            Optional<PaymentMethod> paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId());
            if (!paymentMethod.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("paymentMethod not found"));
            }
        }



        // Additional validation logic if needed...

        return ResponseEntity.ok(StandardResponse.success("Refund request validated successfully"));
    }

}
