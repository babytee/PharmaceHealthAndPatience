package com.pharmacy.intelrx.admin.controllers;

import com.pharmacy.intelrx.admin.services.SupportTicketService;
import com.pharmacy.intelrx.pharmacy.dto.FilterSalesRequest;
import com.pharmacy.intelrx.pharmacy.dto.SupportTicketFilterRequest;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/admin/tickets"})
@RestController("AdminSupportTicketController")
public class SupportTicketController {
    private final SupportTicketService supportTicketService;

    @GetMapping({"overview"})
    public ResponseEntity<?> ticketsOverview() {
        return supportTicketService.ticketsOverview();
    }


    @GetMapping("/filters_support_ticket")
    public ResponseEntity<?> filtersSupportTicket(
            @RequestParam(name = "intelRxId", required = false) String intelRxId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "supportTypeId", required = false) Long supportTypeId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "50") int pageSize) {

        // ...
        SupportTicketFilterRequest supportTicketFilterRequest = new SupportTicketFilterRequest();
        supportTicketFilterRequest.setIntelRxId(intelRxId);
        supportTicketFilterRequest.setSupportTypeId(supportTypeId);
        supportTicketFilterRequest.setState(state);
        supportTicketFilterRequest.setKeyword(keyword);

        // Adjust the page number if it is 1
        int adjustedPage = (page <= 1) ? 0 : page - 1;

        // Pass the pagination parameters to the service method
        Pageable pageable = PageRequest.of(adjustedPage, pageSize);

        return supportTicketService.filtersSupportTicket(supportTicketFilterRequest, pageable);
    }


    @GetMapping("/fetch_ticket")
    public ResponseEntity<?> fetchTicket(
            @RequestParam(name = "intelRxId", required = true) String intelRxId,
            @RequestParam(name = "supportId", required = true) Long supportId) throws IOException {

        // ...
        SupportTicketFilterRequest supportTicketFilterRequest = new SupportTicketFilterRequest();
        supportTicketFilterRequest.setIntelRxId(intelRxId);
        supportTicketFilterRequest.setId(supportId);

        return supportTicketService.fetchTicket(supportTicketFilterRequest);
    }


    @PatchMapping("change_status/{id}")
    public ResponseEntity<?> changeStatus(@RequestBody SupportTicketFilterRequest request,
                                          @PathVariable Long id) throws MessagingException, UnsupportedEncodingException {
        return supportTicketService.changeStatus(request, id);
    }


    @GetMapping("yearly_analytics")
    public ResponseEntity<?> yearlyAnalytics(
            @RequestParam(name = "year", required = false, defaultValue = "0") int year
    ) {
        return supportTicketService.yearlyAnalytics(year);
    }

    @GetMapping("weekly_analytics")
    public ResponseEntity<?> weeklyAnalytics(
            @RequestParam(name = "start_date", required =true) String start_date,
            @RequestParam(name = "end_date", required = true) String end_date
    ) {
        return supportTicketService.weeklyAnalytics(start_date,end_date);
    }

    @GetMapping("top_complaint")
    public ResponseEntity<?>topComplaint(){
        return supportTicketService.topComplaint();
    }
}
