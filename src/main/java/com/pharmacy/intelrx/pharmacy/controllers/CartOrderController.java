package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.CartItemRequest;
import com.pharmacy.intelrx.pharmacy.dto.OrderRequest;
import com.pharmacy.intelrx.pharmacy.services.CartOrderServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy"})
@RestController("CartOrderController")
public class CartOrderController {
    private final CartOrderServices cartOrderServices;

    @PostMapping("add_to_cart")
    public ResponseEntity<?> addItemToCart(@RequestBody List<CartItemRequest> cartItemRequests) {
        return cartOrderServices.addItemToCart(cartItemRequests);
    }

    @PostMapping("resell_item_to_cart")
    public ResponseEntity<?> resellItemToCart(@RequestBody List<CartItemRequest> cartItemRequests) {
        return cartOrderServices.resellItemToCart(cartItemRequests);
    }

    @GetMapping("get_cart_item")
    public ResponseEntity<?> getCartItem() {
        return cartOrderServices.getCartItem();
    }

    @GetMapping("remove_cart_order/{orderId}")
    public ResponseEntity<?> removeCartOrder(@PathVariable Long orderId) {
        return cartOrderServices.removeCartOrder(orderId);
    }


    @GetMapping("transfer_order")
    public ResponseEntity<?> transferOrder(
            @RequestParam(name = "orderId", required = false) Long orderId) {
        return cartOrderServices.transferOrder(orderId);
    }

    @PostMapping("submit_order")
    public ResponseEntity<?> submitOrder(@RequestBody OrderRequest request) {
        return cartOrderServices.submitOrder(request);
    }
    @PostMapping("submit_resell_order")
    public ResponseEntity<?> submitResellOrder(@RequestBody OrderRequest request) {
        return cartOrderServices.submitResellOrder(request);
    }

    @GetMapping("get_queued_items")
    public ResponseEntity<?> getQueuedItems() {
        return cartOrderServices.getQueuedItems();
    }

    @GetMapping("get_queued_item_details/{orderId}")
    public ResponseEntity<?> getQueuedItemDetails(@PathVariable Long orderId) {
        return cartOrderServices.getQueuedItemDetails(orderId);
    }

    @GetMapping("search_patients_from_order")
    public ResponseEntity<?> searchPatients(@RequestParam(name = "keyword") String keyword) {
        return cartOrderServices.searchPatientsFromOrder(keyword);
    }
}
