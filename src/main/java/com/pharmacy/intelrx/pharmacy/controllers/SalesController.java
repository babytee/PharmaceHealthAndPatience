package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.FilterSalesRequest;
import com.pharmacy.intelrx.pharmacy.dto.OrderRefundRequest;
import com.pharmacy.intelrx.pharmacy.services.OrderRefundService;
import com.pharmacy.intelrx.pharmacy.services.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy"})
@RestController("SalesController")
public class SalesController {
    private final SalesService salesService;
    private final OrderRefundService orderRefundService;

    @GetMapping("/sales_history_details")
    public ResponseEntity<?> salesHistoryDetails(
            @RequestParam(name = "orderId", required = true) Long orderId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "50") int pageSize) {

        // Adjust the page number if it is 1
        int adjustedPage = (page <= 1) ? 0 : page - 1;

        // Pass the pagination parameters to the service method
        Pageable pageable = PageRequest.of(adjustedPage, pageSize);
        return salesService.salesHistoryDetails(orderId, pageable);
    }

    @GetMapping("get_single_order/{orderId}")
    public ResponseEntity<?> getSingleSalesOrder(@PathVariable Long orderId) {
        return salesService.getSingleSalesOrder(orderId);
    }

    @GetMapping("view_receipt/{orderId}")
    public ResponseEntity<?> viewReceipt(@PathVariable Long orderId) {
        return salesService.viewReceipt(orderId);
    }

    @GetMapping("/filtered_sales_orders")
    public ResponseEntity<?> getFilteredOrders(
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
        filterSalesRequest.setDateFilter(dateFilter);

        // Adjust the page number if it is 1
        int adjustedPage = (page <= 1) ? 0 : page - 1;

        // Pass the pagination parameters to the service method
        Pageable pageable = PageRequest.of(adjustedPage, pageSize);

        return salesService.getFilteredOrdersSalesHistory(filterSalesRequest, pageable);
    }

    @PostMapping("issue_refund")
    public ResponseEntity<?> issueRefund(@RequestBody OrderRefundRequest request) {
        return orderRefundService.issueRefund(request);
    }

    @PostMapping("issue_item_refund")
    public ResponseEntity<?> issueItemRefund(@RequestBody List<OrderRefundRequest> request) {
        return orderRefundService.issueItemRefund(request);
    }

    @GetMapping("sales_stats")
    public ResponseEntity<?> salesStats(
            @RequestParam(name = "dateFilter", required = false) String dateFilter) {
        FilterSalesRequest filterSalesRequest = new FilterSalesRequest();
        filterSalesRequest.setDateFilter(dateFilter);
        return salesService.salesStats(filterSalesRequest);
    }


}
