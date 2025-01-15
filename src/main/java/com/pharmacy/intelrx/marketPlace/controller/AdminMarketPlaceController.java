package com.pharmacy.intelrx.marketPlace.controller;

import com.pharmacy.intelrx.marketPlace.dto.PackageStatusRequest;
import com.pharmacy.intelrx.marketPlace.service.AdminMarketPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/admin/marketplace"})
@RestController("AdminMarketPlaceController")
public class AdminMarketPlaceController {
    private final AdminMarketPlaceService marketPlaceService;

    @GetMapping({"search_market_place"})
    public ResponseEntity<?> searchMarketPlace(
            @RequestParam String searchParam,
            @RequestParam(required = false) String brandName,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return marketPlaceService.searchMarketPlace(searchParam, brandName, pageable);
    }


    @GetMapping({"get_order_history"})
    public ResponseEntity<?> getOrderHistory(
            @RequestParam(required = true) String intelRxId,
            @RequestParam(required = false) String orderRef,
            @RequestParam(required = false) String salesStatus,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return marketPlaceService.getOrderHistory(intelRxId,orderRef, salesStatus, pageable);
    }

    @GetMapping({"get_single_order_history/{orderId}"})
    public ResponseEntity<?> getOrderHistory(@PathVariable Long orderId) {
        return marketPlaceService.getSingleOrderHistory(orderId);
    }

    @GetMapping({"get_market_order_cart"})
    public ResponseEntity<?> getMarketOrderCartItem(
            @RequestParam Long orderId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return marketPlaceService.getMarketOrderCartItem(orderId, pageable);
    }

    @PostMapping("manage_package_status")
    public ResponseEntity<?> managePackageStatus(@RequestBody PackageStatusRequest request) {
        return marketPlaceService.managePackageStatus(request);
    }

    @GetMapping({"get_package_history/{cartItemId}/{supplierIntelRxId}"})
    public ResponseEntity<?> getPackageHistory(
            @PathVariable Long cartItemId,
            @PathVariable String supplierIntelRxId) {
        return marketPlaceService.getPackageHistory(cartItemId,supplierIntelRxId);
    }


    @GetMapping("over_view_stat")
    public ResponseEntity<?> overViewStat() {
        return marketPlaceService.overViewStat();
    }

    @GetMapping({"get_pending_order_item"})
    public ResponseEntity<?> getPendingOrderItem(
            @RequestParam(required = true) String intelRxId,
            @RequestParam(required = false) String salesStatus,
            @RequestParam(required = false) String search,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return marketPlaceService.getPendingOrderItem(intelRxId,salesStatus, search, pageable);
    }

}
