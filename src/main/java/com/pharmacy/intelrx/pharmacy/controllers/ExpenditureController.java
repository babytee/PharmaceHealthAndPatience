package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.pharmacy.dto.ExpenditureRequest;
import com.pharmacy.intelrx.pharmacy.services.ExpenditureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy"})
@RestController("ExpenditureController")
public class ExpenditureController {
    private final ExpenditureService expenditureService;

    @PostMapping({"add_expense"})
    public ResponseEntity<?> addExpense(@RequestBody ExpenditureRequest request) {
        return expenditureService.addExpense(request);
    }

    @PatchMapping({"update_expense/{expenseId}"})
    public ResponseEntity<?> updateExpense(@RequestBody ExpenditureRequest request, @PathVariable Long expenseId) {
        return expenditureService.updateExpense(request, expenseId);
    }

    @GetMapping({"approve_expense/{expenseId}"})
    public ResponseEntity<?> approveExpense(@PathVariable Long expenseId) {
        return expenditureService.approveExpense(expenseId);
    }

    @GetMapping({"expenses"})
    public ResponseEntity<?> allExpenses(
            @RequestParam(required = false) String expenseName,
            @RequestParam(required = false) Long branchId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return expenditureService.allExpenses(expenseName, branchId, pageable);
    }

}
