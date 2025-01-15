package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.pharmacy.dto.*;
import com.pharmacy.intelrx.pharmacy.services.OrganizationSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/org"})
@RestController("OrganizationSettingsController")
public class OrganizationSettingsController {
    private final OrganizationSettingsService organizationSettingsService;

    @PatchMapping({"update"})
    public ResponseEntity<?>updatePharmacy(@RequestBody PharmacyRequest request) throws IOException {
        return organizationSettingsService.updatePharmacy(request);
    }

    @GetMapping({"details"})
    public ResponseEntity<?>pharmacyDetails() throws IOException {
        return organizationSettingsService.pharmacyDetails();
    }

    @GetMapping({"managers"})
    public ResponseEntity<?>getManagers(){
        return organizationSettingsService.getManagers();
    }

    @GetMapping({"get_roles"})
    public ResponseEntity<?>getRoles(){
        return organizationSettingsService.getRoles();
    }

    @PostMapping({"manage_bank_info"})
    public ResponseEntity<?>manageBankInfo(@RequestBody BankInfoRequest request){
        return organizationSettingsService.addNewBankInfo(request);
    }

    @PatchMapping({"update_bank_info"})
    public ResponseEntity<?>updateBankInfo(@RequestBody BankInfoRequest request,@PathVariable Long id){
        return organizationSettingsService.updateBankInfo(request,id);
    }

    @GetMapping({"get_bank_info"})
    public ResponseEntity<?>getBankInfo(){
        return organizationSettingsService.getBankInfo();
    }

    @PostMapping({"manage_sales_margin"})
    public ResponseEntity<?>manageSalesMargin(@RequestBody SalesMarginRequest request) {
        return organizationSettingsService.manageSalesMargin(request);
    }

    @GetMapping({"get_sales_margin"})
    public ResponseEntity<?>getSalesMargin(){
        return organizationSettingsService.getSalesMargin();
    }

    @PostMapping({"manage_discount"})
    public ResponseEntity<?>manageDiscount(@RequestBody DiscountRequest request){
        return organizationSettingsService.manageDiscount(request);
    }

    @GetMapping({"get_discount"})
    public ResponseEntity<?>getDiscount(){
        return organizationSettingsService.getDiscount();
    }

    @PostMapping({"manage_stock_expiration"})
    public ResponseEntity<?>manageOutOfStockExpiration(@RequestBody OutOfStockNotificationRequest request){
        return organizationSettingsService.manageOutOfStockExpiration(request);
    }

    @GetMapping({"get_stock_expiration"})
    public ResponseEntity<?>getOutOfStockExpiration(){
        return organizationSettingsService.getOutOfStockExpiration();
    }

    @PostMapping({"manage_drug_expiration"})
    public ResponseEntity<?>manageDrugExpiration(@RequestBody DrugExpirationNotificationRequest request){
        return organizationSettingsService.manageDrugExpiration(request);
    }

    @GetMapping({"get_drug_expiration"})
    public ResponseEntity<?>getDrugExpiration(){
        return organizationSettingsService.getDrugExpiration();
    }
}
