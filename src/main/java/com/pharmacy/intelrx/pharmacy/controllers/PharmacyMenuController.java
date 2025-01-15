package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.pharmacy.services.PharmacyMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy/menu"})
@RestController("PharmacyMenuController")
public class PharmacyMenuController {
    private final PharmacyMenuService pharmacyMenuService;

    @GetMapping("expense_menu_count")
    public ResponseEntity<?> expenseMenuCount() {
        return pharmacyMenuService.expenseMenuCount();
    }

    @GetMapping("queued_item_menu_count")
    public ResponseEntity<?> queuedItemMenuCount() {
        return pharmacyMenuService.queuedItemMenuCount();
    }

}
