package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.generalCoupon.dto.GeneralCouponRequest;
import com.pharmacy.intelrx.pharmacy.services.GeneralCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController("PharmacyGeneralCouponController")
@RequestMapping({"/api/v1/user/pharmacy/general_coupon"})
public class GeneralCouponController {
    private final GeneralCouponService generalCouponService;

    @PostMapping("create")
    public ResponseEntity<?> createGeneralCoupon(@RequestBody
                                                 GeneralCouponRequest request) {
        return generalCouponService.createGeneralCoupon(request);
    }

    @PatchMapping("update/{generalCouponId}")
    public ResponseEntity<?> updateGeneralCoupon(@RequestBody
                                                 GeneralCouponRequest request,
                                                 @PathVariable Long generalCouponId) {
        return generalCouponService.updateGeneralCoupon(request, generalCouponId);
    }

    @PatchMapping("disable_coupon/{generalCouponId}")
    public ResponseEntity<?> disableCoupon(@PathVariable Long generalCouponId) {
        return generalCouponService.disableCoupon(generalCouponId);
    }

    @GetMapping
    public ResponseEntity<?> generalCoupons(
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "couponStatus", required = false) String couponStatus,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return generalCouponService.generalCoupons
                (couponStatus, searchTerm, sortBy, pageable);
    }

    @GetMapping("in_house")
    public ResponseEntity<?> inHouseCoupons(
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "couponStatus", required = false) String couponStatus,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return generalCouponService.inHouseCoupons
                (couponStatus, searchTerm, sortBy, pageable);
    }

    @GetMapping("coupon_details")
    public ResponseEntity<?> getCouponDetails(
            @RequestParam(value = "couponCode", required = true) String couponCode,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return generalCouponService.getCouponDetails
                (couponCode, pageable);
    }

    @GetMapping("coupon_order_receipt")
    public ResponseEntity<?> viewOrderReceipt(
            @RequestParam(value = "couponCode", required = true) String couponCode,
            @RequestParam(value = "orderId", required = true) Long orderId
    ) {
        return generalCouponService.viewOrderReceipt
                (couponCode, orderId);
    }

    @GetMapping("check_coupon_code")
    public ResponseEntity<?> checkCouponCode(
            @RequestParam(value = "couponCode", required = true) String couponCode,
            @RequestParam(value = "couponType", required = true) String couponType
    ) {
        return generalCouponService.checkCouponCode
                (couponCode, couponType);
    }

    @GetMapping("transaction_history")
    public ResponseEntity<?> transactionHistory(
            @RequestParam(value = "dateFrom", required = false) LocalDateTime dateFrom,
            @RequestParam(value = "dateTo", required = false) LocalDateTime dateTo,
            @RequestParam(value = "trxCouponType", required = false) String trxCouponType,
            @RequestParam(value = "trxType", required = false) String trxType,
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return generalCouponService.transactionHistory
                (dateFrom, dateTo, trxCouponType,trxType,
                        searchTerm, pageable);
    }

    @GetMapping("general_coupon_stat")
    public ResponseEntity<?> generalCouponStat() {
        return generalCouponService.generalCouponStat();
    }

}
