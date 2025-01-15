package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.pharmacy.dto.inventory.SupplierPaymentRequest;
import com.pharmacy.intelrx.pharmacy.dto.inventory.SupplierRequest;
import com.pharmacy.intelrx.pharmacy.services.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/pharmacy"})
@RestController("SupplierController")
public class SupplierController {
    private final SupplierService supplierService;

    @PostMapping("add_new_supplier")
    public ResponseEntity<?> addNewSupplier(@RequestBody SupplierRequest request) {
        return supplierService.addNewSupplier(request);
    }

    @PostMapping("add_supplier_payment")
    public ResponseEntity<?> addSupplierToInventory(@RequestBody SupplierPaymentRequest request) {
        return supplierService.addSupplierToInventory(request);
    }

    @PatchMapping("update_supplier_payment/{supplierId}")
    public ResponseEntity<?> updateSupplierToInventory(@RequestBody SupplierPaymentRequest request, @PathVariable Long supplierId) throws IOException {
        return supplierService.updateSupplierToInventory(request, supplierId);
    }

    @GetMapping({"fetch_all_supplier"})
    public ResponseEntity<?> suppliers() {
        return supplierService.suppliers();
    }


    @GetMapping({"supplier_by_payment_status/{paymentStatusId}"})
    public ResponseEntity<?> SuppliersByPaymentStatus(@PathVariable Long paymentStatusId) {
        return supplierService.SuppliersByPaymentStatus(paymentStatusId);
    }


    @GetMapping({"supplier_details/{supplierId}"})
    public ResponseEntity<?> supplierDetails(@PathVariable Long supplierId) throws IOException {
        return supplierService.supplierDetails(supplierId);
    }

    @GetMapping({"supplier_payments/{supplierId}"})
    public ResponseEntity<?> supplierPayments(@PathVariable Long supplierId) {
        return supplierService.supplierPayments(supplierId);
    }

    @GetMapping({"supplier_payment/{InvoiceRefNumber}"})
    public ResponseEntity<?> supplierPayment(@PathVariable String InvoiceRefNumber) throws IOException {
        return supplierService.supplierPayment(InvoiceRefNumber);
    }

    @GetMapping({"supplier_invoice"})
    public ResponseEntity<?> supplierInvoice(
            @RequestParam String InvoiceRefNumber,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        // Adjust the page to start from 0 if the user provides 1
        page = (page < 1) ? 0 : page - 1;

        Pageable pageable = PageRequest.of(page, size);
        return supplierService.supplierPaymentInvoice(InvoiceRefNumber, pageable);
    }


    @GetMapping({"search_suppliers"})
    public ResponseEntity<?> searchSuppliers(@RequestParam(required = false) String searchTerm) throws IOException {
        return supplierService.searchSuppliers(searchTerm);
    }

}
