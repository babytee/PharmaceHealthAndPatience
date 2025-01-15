package com.pharmacy.intelrx.admin.controllers;

import com.pharmacy.intelrx.admin.services.ExpenditureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/admin/pharmacy"})
@RestController("AdminExpenditureController")
public class ExpenditureController {
    private final ExpenditureService expenditureService;


    @GetMapping({"expenses"})
    public ResponseEntity<?>allExpenses(
            @RequestParam(required = false) String expenseName,
            @RequestParam(required = false) String intelRxId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size){
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return expenditureService.allExpenses(expenseName,intelRxId,branchId,pageable);
    }

}
