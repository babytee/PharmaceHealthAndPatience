package com.pharmacy.intelrx.admin.controllers;

import com.pharmacy.intelrx.admin.services.GeneralCouponService;
import com.pharmacy.intelrx.generalCoupon.dto.GeneralCouponRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController("adminGeneralCouponController")
@RequestMapping({"/api/v1/admin/general_coupon"})
public class GeneralCouponController {
    private final GeneralCouponService generalCouponService;

    @PostMapping("create")
    public ResponseEntity <?> createGeneralCoupon(@RequestBody
                                                  GeneralCouponRequest request) {
        return generalCouponService.createGeneralCoupon(request);
    }

    @GetMapping("all_coupons")
    public ResponseEntity<?> allCoupons(
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "couponStatus", required = false) String couponStatus,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return generalCouponService.allCoupons
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


}
