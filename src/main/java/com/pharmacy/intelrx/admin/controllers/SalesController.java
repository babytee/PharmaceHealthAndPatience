package com.pharmacy.intelrx.admin.controllers;

import com.pharmacy.intelrx.admin.services.SalesService;
import com.pharmacy.intelrx.pharmacy.dto.FilterSalesRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/admin/pharmacy"})
@RestController("AdminSalesController")
public class SalesController {
    private final SalesService salesService;


    @GetMapping("/sales_history_details")
    public ResponseEntity<?> salesHistoryDetails(
            @RequestParam(name = "orderId", required = true) Long orderId,
            @RequestParam(name = "intelRxId", required = true) String intelRxId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

        // Adjust the page number if it is 1
        int adjustedPage = (page <= 1) ? 0 : page - 1;

        // Pass the pagination parameters to the service method
        Pageable pageable = PageRequest.of(adjustedPage, pageSize);
        return salesService.salesHistoryDetails(orderId,intelRxId, pageable);
    }

    @GetMapping("get_single_order/{orderId}")
    public ResponseEntity<?> getSingleSalesOrder(@PathVariable Long orderId) {
        return salesService.getSingleSalesOrder(orderId);
    }

    @GetMapping("view_receipt/{orderId}")
    public ResponseEntity<?> viewReceipt(@PathVariable Long orderId) {
        return salesService.viewReceipt(orderId);
    }


    @GetMapping("sales_stats/{intelRxId}")
    public ResponseEntity<?> salesStats(@PathVariable String intelRxId) {
        return salesService.salesStats(intelRxId);
    }

    @GetMapping("/filtered_sales_orders")
    public ResponseEntity<?> getFilteredOrders(
            @RequestParam(name = "intelRxId", required = true) String intelRxId,
            @RequestParam(name = "branchId", required = false) Long branchId,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "salesPersonId", required = false) Long salesPersonId,
            @RequestParam(name = "cashierId", required = false) Long cashierId,
            @RequestParam(name = "salesStatus", required = false) String salesStatus,
            @RequestParam(name = "dateFilter", required = false) String dateFilter,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

        // ...
        FilterSalesRequest filterSalesRequest = new FilterSalesRequest();
        filterSalesRequest.setSalesStatus(salesStatus);
        filterSalesRequest.setCashierId(cashierId);
        filterSalesRequest.setBranchId(branchId);
        filterSalesRequest.setStartDate(startDate);
        filterSalesRequest.setEndDate(endDate);
        filterSalesRequest.setSalesPersonId(salesPersonId);
        filterSalesRequest.setIntelRxId(intelRxId);
        filterSalesRequest.setDateFilter(dateFilter);

        // Adjust the page number if it is 1
        int adjustedPage = (page <= 1) ? 0 : page - 1;

        // Pass the pagination parameters to the service method
        Pageable pageable = PageRequest.of(adjustedPage, pageSize);

        return salesService.getFilteredOrdersSalesHistory(filterSalesRequest, pageable);
        //return  ResponseEntity.ok(StandardResponse.success(startDate));
    }



}
