package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.pharmacy.services.PharmacyAnalyticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy/analytics"})
@RestController("PharmacyAnalyticController")
public class PharmacyAnalyticController {
    private final PharmacyAnalyticService pharmacyAnalyticService;

    @GetMapping("monthly_sales_revenue")
    public ResponseEntity<?> monthlySalesRevenue(
            @RequestParam(name = "duration", required = false) String duration,
            @RequestParam(name = "year", required = false,defaultValue = "0") int year,
            @RequestParam(name = "month", required = false,defaultValue = "0") int month,
            @RequestParam(name = "branchId", required = false) Long branchId
    ) {
        return pharmacyAnalyticService.monthlySalesRevenue(year,month,duration,branchId);
    }

    @GetMapping("yearly_revenue_insight")
    public ResponseEntity<?> yearlyRevenueInsight(
            @RequestParam(name = "year", required = false,defaultValue = "0") int year,
            @RequestParam(name = "branchId", required = false) Long branchId
    ) {
        return pharmacyAnalyticService.yearlyRevenueInsight(year,branchId);
    }

    @GetMapping("weekly_revenue_insight")
    public ResponseEntity<?> rangeRevenueInsight(
            @RequestParam(name = "start_date", required =true) String start_date,
            @RequestParam(name = "end_date", required = true) String end_date,
            @RequestParam(name = "branchId", required = false) Long branchId
    ) {
        return pharmacyAnalyticService.rangeRevenueInsight(start_date,end_date,branchId);
    }

    @GetMapping("/top_selling")
    public ResponseEntity<?> getTopSellingInventoryItems() {
        return pharmacyAnalyticService.topSellingProducts();
    }

//
//    //Expenditure
    @GetMapping("monthly_exp_revenue")
    public ResponseEntity<?> monthlyExpenditureRevenue(
            @RequestParam(name = "duration", required = false) String duration,
            @RequestParam(name = "year", required = false,defaultValue = "0") int year,
            @RequestParam(name = "month", required = false,defaultValue = "0") int month,
            @RequestParam(name = "branchId", required = false) Long branchId
    ) {
        return pharmacyAnalyticService.monthlyExpenditureRevenue(year,month,duration,branchId);
    }


    @GetMapping("yearly_exp_insight")
    public ResponseEntity<?> yearlyExpenditureInsight(
            @RequestParam(name = "year", required = false,defaultValue = "0") int year,
            @RequestParam(name = "branchId", required = false) Long branchId
    ) {
        return pharmacyAnalyticService.yearlyExpenditureInsight(year,branchId);
    }



    @GetMapping("weekly_exp_insight")
    public ResponseEntity<?> rangeExpenditureRevenueInsight(
            @RequestParam(name = "start_date", required =true) String start_date,
            @RequestParam(name = "end_date", required = true) String end_date,
            @RequestParam(name = "branchId", required = false) Long branchId
    ) {
        return pharmacyAnalyticService.rangeExpenditureRevenueInsight(start_date,end_date,branchId);
    }

    @GetMapping("/miscellaneous_exp")
    public ResponseEntity<?> miscellaneousExpenditure() {
        return pharmacyAnalyticService.miscellaneousExpenditure();
    }


}
