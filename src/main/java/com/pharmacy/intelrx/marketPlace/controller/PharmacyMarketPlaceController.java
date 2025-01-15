package com.pharmacy.intelrx.marketPlace.controller;

import com.pharmacy.intelrx.marketPlace.dto.MarketPlaceCartItemRequest;
import com.pharmacy.intelrx.marketPlace.dto.OrderBillInfoRequest;
import com.pharmacy.intelrx.marketPlace.dto.PackageStatusRequest;
import com.pharmacy.intelrx.marketPlace.service.PharmacyMarketPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy/mp"})
@RestController("PharmacyMarketPlaceController")
public class PharmacyMarketPlaceController {
    private final PharmacyMarketPlaceService pharmacyMarketPlaceService;

    @GetMapping({"search_market_place"})
    public ResponseEntity<?> searchMarketPlace(
            @RequestParam String searchParam,
            @RequestParam(required = false) String brandName,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return pharmacyMarketPlaceService.searchMarketPlace(searchParam, brandName, pageable);
    }

    @PostMapping("add_to_cart")
    public ResponseEntity<?> addItemToCart(@RequestBody List<MarketPlaceCartItemRequest> cartItemRequests) {
        return pharmacyMarketPlaceService.addItemToCart(cartItemRequests);
    }

    @GetMapping("get_cart_item")
    public ResponseEntity<?> getCartItem() {
        return pharmacyMarketPlaceService.getCartItem();
    }

    @PostMapping("submit_order")
    public ResponseEntity<?> submitOrder(@RequestBody OrderBillInfoRequest request) {
        return pharmacyMarketPlaceService.submitOrder(request);
    }

    @GetMapping({"get_order_history"})
    public ResponseEntity<?> getOrderHistory(
            @RequestParam(required = false) String orderRef,
            @RequestParam(required = false) String salesStatus,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return pharmacyMarketPlaceService.getOrderHistory(orderRef, salesStatus, pageable);
    }

    @GetMapping({"get_single_order_history/{orderId}"})
    public ResponseEntity<?> getOrderHistory(@PathVariable Long orderId) {
        return pharmacyMarketPlaceService.getSingleOrderHistory(orderId);
    }

    @GetMapping({"get_market_order_cart"})
    public ResponseEntity<?> getMarketOrderCartItem(
            @RequestParam Long orderId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return pharmacyMarketPlaceService.getMarketOrderCartItem(orderId, pageable);
    }

    @PostMapping("manage_package_status")
    public ResponseEntity<?> managePackageStatus(@RequestBody PackageStatusRequest request) throws IOException {
        return pharmacyMarketPlaceService.managePackageStatus(request);
    }

    @GetMapping({"get_package_history/{cartItemId}/{supplierIntelRxId}"})
    public ResponseEntity<?> getPackageHistory(
            @PathVariable Long cartItemId,
            @PathVariable String supplierIntelRxId) {
        return pharmacyMarketPlaceService.getPackageHistory(cartItemId,supplierIntelRxId);
    }

    @GetMapping({"my_sales"})
    public ResponseEntity<?> mySales(
            @RequestParam(required = false) String salesStatus,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return pharmacyMarketPlaceService.mySales(salesStatus, pageable);
    }

    @GetMapping("over_view_stat")
    public ResponseEntity<?> overViewStat() {
        return pharmacyMarketPlaceService.overViewStat();
    }

    @GetMapping({"get_pending_order_item"})
    public ResponseEntity<?> getPendingOrderItem(
            @RequestParam(required = false) String salesStatus,
            @RequestParam(required = false) String search,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return pharmacyMarketPlaceService.getPendingOrderItem(salesStatus, search, pageable);
    }

    @GetMapping("/top_selling")
    public ResponseEntity<?> getTopSellingInventoryItems() {
        return pharmacyMarketPlaceService.topSellingProducts();
    }

    @GetMapping("market_report")
    public ResponseEntity<?> marketReport() {
        return pharmacyMarketPlaceService.marketReport();
    }


}
