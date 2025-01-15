package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.auxilliary.dto.Response;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.repositories.PaymentStatusRepository;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.ExpenditureRequest;
import com.pharmacy.intelrx.pharmacy.dto.inventory.InventoryResponse;
import com.pharmacy.intelrx.pharmacy.dto.inventory.SupplierPaymentRequest;
import com.pharmacy.intelrx.pharmacy.dto.inventory.SupplierRequest;
import com.pharmacy.intelrx.pharmacy.dto.inventory.SupplierResponse;
import com.pharmacy.intelrx.pharmacy.models.*;
import com.pharmacy.intelrx.pharmacy.repositories.*;
import com.pharmacy.intelrx.pharmacy.utility.FilterInventory;
import com.pharmacy.intelrx.pharmacy.utility.InventoryValidation;
import com.pharmacy.intelrx.pharmacy.utility.SupplierMapping;
import com.pharmacy.intelrx.utility.UserDetailsService;
import com.pharmacy.intelrx.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("SupplierService")
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final Utility utility;
    private final SupplierMapping supplierMapping;
    private final InventoryValidation inventoryValidation;
    private final InventoryRepository inventoryRepository;
    private final S3Service s3Service;
    private final PharmacyRepository pharmacyRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final UserDetailsService userDetailsService;
    private final SupplierPaymentRepository supplierPaymentRepository;
    private final FilterInventory filterInventory;
    private final ExpenditureService expenditureService;
    private final SupplierPaymentHistoryRepository paymentHistoryRepository;

    public ResponseEntity<?> suppliers() {
        User user = userDetailsService.getAuthenticatedUser();
        String intelRxId = userDetailsService.getIntelRxId();
        List<Supplier> supplierList = supplierRepository.findAllByIntelRxId(intelRxId);

        if (supplierList == null || supplierList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(supplierList));
        }

        List<SupplierResponse> responseList = supplierList.stream().map(supplier ->
        {
            try {
                return supplierMapping.mapToSupplierResponse(supplier);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(responseList));

    }

    public ResponseEntity<?> SuppliersByPaymentStatus(Long paymentStatusId) {
        String intelRxId = userDetailsService.getIntelRxId();

        List<SupplierPayment> supplierList = null;
        if (paymentStatusId == 0) {
            supplierList = supplierPaymentRepository.findAllByIntelRxId(intelRxId);
        } else {
            var paymentStatus = paymentStatusRepository.findById(paymentStatusId)
                    .orElse(null);
            if (paymentStatus == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("paymentStatusId not found"));
            }

            if (paymentStatusId == 1) {
                supplierList = supplierPaymentRepository.findAllByIntelRxIdAndPaymentStatus(intelRxId, paymentStatus);
            }
            if (paymentStatusId == 2) {
                supplierList = supplierPaymentRepository.findAllByIntelRxIdAndPaymentStatus(intelRxId, paymentStatus);
            }
        }

        if (supplierList == null || supplierList.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("paymentStatusId not found "));
        }

        List<SupplierResponse> responseList = supplierList.stream().map(supplier ->
        {
            return supplierMapping.mapToSupplierPaymentResponse(supplier);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(responseList));

    }

    public ResponseEntity<?> searchSuppliers(String searchTerm) {
        String intelRxId = userDetailsService.getIntelRxId();

        List<Supplier> supplierList = supplierRepository.searchSuppliersByAnyFieldAndIntelRxId
                (searchTerm, intelRxId);

        if (supplierList == null || supplierList.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("No supplier found "));
        }

        List<SupplierResponse> responseList = supplierList.stream().map(supplier ->
        {
            try {
                return supplierMapping.mapToSupplierResponse(supplier);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(responseList));

    }

    public ResponseEntity<?> supplierDetails(Long supplierId) throws IOException {
        if (supplierId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("supplierId is required"));
        }

        Optional<Supplier> optional = supplierRepository.findById(supplierId);
        if (optional == null || optional.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("supplierId not found"));
        }

        SupplierResponse responseList = supplierMapping.mapToSupplierResponse(optional.get());

        return ResponseEntity.ok(StandardResponse.success(responseList));

    }

    public ResponseEntity<?> addNewSupplier(SupplierRequest request) {
        var user = userDetailsService.getAuthenticatedUser();
        if (utility.isNullOrEmpty(request.getName())) {
            return ResponseEntity.badRequest().body(
                    StandardResponse.error("name is required")
            );
        }

        SupplierRequest supplierRequest = new SupplierRequest();
        supplierRequest.setName(request.getName());
        supplierRequest.setPhoneNumber(request.getPhoneNumber());
        Supplier sup = newSupplier(supplierRequest, null);

        SupplierRequest supplierResponse = new SupplierRequest();
        supplierResponse.setId(sup.getId());
        supplierResponse.setName(sup.getName());
        supplierResponse.setPhoneNumber(sup.getPhoneNumber());

        return ResponseEntity.ok(StandardResponse.success("Supplier Added Successfully", supplierResponse));
    }

    public Supplier newSupplier(SupplierRequest request, Long supplierId) {
        User user;
        String intelRxId;

        if (request.getIntelRxId() != null) {
            intelRxId = request.getIntelRxId();
            user = request.getUser();
        } else {
            intelRxId = userDetailsService.getIntelRxId();
            user = userDetailsService.getAuthenticatedUser();
        }

        Supplier supplier = null;

        // Check if supplierId is provided, then find by ID
        if (supplierId != null) {
            supplier = supplierRepository.findById(supplierId).orElse(null);
        }

        // If no supplier is found by ID, try finding by supplierPharmacy
        if (supplier == null && request.getSupplierPharmacy() != null) {
            supplier = supplierRepository.findBySupplierPharmacy(request.getSupplierPharmacy()).orElse(null);
        }

        // Update or create supplier based on existence
        if (supplier != null) {
            supplier.setName(request.getName());
            supplier.setPhoneNumber(request.getPhoneNumber());
        } else {
            supplier = new Supplier();
            supplier.setName(request.getName());
            supplier.setPhoneNumber(request.getPhoneNumber());
            supplier.setUser(user);
            supplier.setIntelRxId(intelRxId);
            supplier.setSupplierPharmacy(request.getSupplierPharmacy());
            supplier.setCreatedAt(LocalDateTime.now());
        }

        return supplierRepository.save(supplier);
    }

    public void addSupplierPaymentHistory(SupplierPaymentRequest request, SupplierPayment supplierPayment) {
        var user = userDetailsService.getAuthenticatedUser();
        var intelRxId = userDetailsService.getIntelRxId();

        var paymentStatus = inventoryValidation.checkPaymentStatusIfNotExists(request.getPaymentStatusId());

        var paymentMethod = inventoryValidation.checkPaymentMethodIfNotExists(request.getPaymentMethodId());

        SupplierPaymentHistory paymentHistory = SupplierPaymentHistory.builder()
                .amountPaid(request.getAmountPaid())
                .intelRxId(intelRxId)
                .purchaseInvoice(supplierPayment.getPurchaseInvoice())
                .paymentStatus(paymentStatus)
                .paymentMethod(paymentMethod)
                .supplierPayment(supplierPayment)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        paymentHistoryRepository.save(paymentHistory);


    }

    public SupplierPayment addOtherSupplierToInventory(SupplierPaymentRequest request) throws IOException {
        User user = null;
        String intelRxId = null;
        if (request.getUser() != null) {
            user = request.getUser();
        } else {
            user = userDetailsService.getAuthenticatedUser();
        }
        if (request.getIntelRxId() != null) {
            intelRxId = request.getIntelRxId();
        } else {
            intelRxId = userDetailsService.getIntelRxId();
        }
        var paymentStatus = inventoryValidation.checkPaymentStatusIfNotExists(request.getPaymentStatusId());

        var paymentMethod = inventoryValidation.checkPaymentMethodIfNotExists(request.getPaymentMethodId());

        String ref = utility.generateUniqueInvoiceRefNumber();
        Optional<SupplierPayment> medInvoice = supplierPaymentRepository.findByInvoiceRefNumber(ref);

        if (medInvoice.isEmpty()) {
            String bankTransferUrl = request.getBankTransfer() != null ?
                    s3Service.uploadFileDoc(request.getBankTransfer(), "pharmacy") : "";

            String purchaseInvoiceUrl = request.getPurchaseInvoice() != null ?
                    s3Service.uploadFileDoc(request.getPurchaseInvoice(), "pharmacy") : "";

            var supplier = supplierRepository.findByIdAndIntelRxId(request.getSupplierId(), intelRxId)
                    .orElse(null);

            if (supplier == null) {
                //if supplier is not found add a new one
                SupplierRequest supplierRequest = new SupplierRequest();
                supplierRequest.setName(request.getName());
                supplierRequest.setPhoneNumber(request.getPhoneNumber());
                supplier = newSupplier(supplierRequest, null);
            }

            //add inventory payment with the supplier receipt to Supplier Payment
            SupplierPayment supplierPayment = SupplierPayment.builder()
                    .intelRxId(intelRxId)
                    .invoiceRefNumber(ref)
                    //.amountPaid(request.getAmountPaid())
                    .paymentMethod(paymentMethod)
                    .supplier(supplier)
                    .paymentDay(request.getPaymentDay())
                    .paymentMonth(request.getPaymentMonth())
                    .paymentYear(request.getPaymentYear())
                    .user(user)
                    .bankTransfer(bankTransferUrl)

                    .paymentStatus(paymentStatus)
                    .invoiceAmount(request.getInvoiceAmount())
                    .totalAmountPaid(request.getAmountPaid())
                    .balanceDue(request.getBalanceDue())
                    .purchaseInvoice(purchaseInvoiceUrl)

                    .dueDay(request.getDueDay())
                    .dueMonth(request.getDueMonth())
                    .dueYear(request.getDueYear())

                    .createdAt(LocalDateTime.now())
                    .build();

            SupplierPayment supplierPayment1 = supplierPaymentRepository.save(supplierPayment);

            Supplier finalSupplier = supplier;

            // Submit expenses if payment day exists
            if (request.getPaymentDay() != null) {
                ExpenditureRequest expenditureRequest = new ExpenditureRequest();
                expenditureRequest.setExpenseName("Inventory Purchase From " + finalSupplier.getName());
                expenditureRequest.setExpDay(request.getPaymentDay());
                expenditureRequest.setExpMonth(request.getPaymentMonth());
                expenditureRequest.setExpYear(request.getPaymentYear());
                expenditureRequest.setAddedBy(user);
                expenditureRequest.setAmountSpent(request.getAmountPaid());
                expenditureService.modifyExpenseEntity(expenditureRequest, null);
            }

            //submit payment history
            addSupplierPaymentHistory(request, supplierPayment1);
            return supplierPayment;
        }

        return null;
    }

    public ResponseEntity<?> addSupplierToInventory(SupplierPaymentRequest request) {
        try {
            var user = userDetailsService.getAuthenticatedUser();

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(StandardResponse.error("You are unauthorized"));
            }

            // Validate supplier request
            ResponseEntity<?> validationResponse = inventoryValidation.validateSupplierRequest(request);
            if (validationResponse.getStatusCode() != HttpStatus.OK) {
                return validationResponse;
            }

            String intelRxId = userDetailsService.getIntelRxId();
            Optional<Pharmacy> optionalPharmacy = pharmacyRepository.findByIntelRxId(intelRxId);
            if (optionalPharmacy.isEmpty()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("intelRxId not found"));
            }

            // Get the unsaved inventory for this user
            List<Inventory> inventoryList = inventoryRepository.findAllByIntelRxIdAndUserAndStatus(intelRxId, user, false);
            if (inventoryList.isEmpty()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("No new inventory list that has not been submitted"));
            }

            var paymentStatus = inventoryValidation.checkPaymentStatusIfNotExists(request.getPaymentStatusId());
            if (paymentStatus == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Payment status not found"));
            }

            var paymentMethod = inventoryValidation.checkPaymentMethodIfNotExists(request.getPaymentMethodId());
            if (paymentMethod == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Payment method not found"));
            }

            String ref = utility.generateUniqueInvoiceRefNumber();
            Optional<SupplierPayment> medInvoice = supplierPaymentRepository.findByInvoiceRefNumber(ref);

            if (medInvoice.isEmpty()) {
                String bankTransferUrl = request.getBankTransfer() != null ?
                        s3Service.uploadFileDoc(request.getBankTransfer(), "pharmacy") : "";

                String purchaseInvoiceUrl = request.getPurchaseInvoice() != null ?
                        s3Service.uploadFileDoc(request.getPurchaseInvoice(), "pharmacy") : "";

                var supplier = supplierRepository.findByIdAndIntelRxId(request.getSupplierId(), intelRxId)
                        .orElse(null);

                if (supplier == null) {
                    //if supplier is not found add a new one
                    SupplierRequest supplierRequest = new SupplierRequest();
                    supplierRequest.setName(request.getName());
                    supplierRequest.setPhoneNumber(request.getPhoneNumber());
                    supplier = newSupplier(supplierRequest, null);
                }

                //add inventory payment with the supplier receipt to Supplier Payment
                SupplierPayment supplierPayment = SupplierPayment.builder()
                        .intelRxId(intelRxId)
                        .invoiceRefNumber(ref)
                        //.amountPaid(request.getAmountPaid())
                        .paymentMethod(paymentMethod)
                        .supplier(supplier)
                        .paymentDay(request.getPaymentDay())
                        .paymentMonth(request.getPaymentMonth())
                        .paymentYear(request.getPaymentYear())
                        .user(user)
                        .bankTransfer(bankTransferUrl)

                        .paymentStatus(paymentStatus)
                        .invoiceAmount(request.getInvoiceAmount())
                        .totalAmountPaid(request.getAmountPaid())
                        .balanceDue(request.getBalanceDue())
                        .purchaseInvoice(purchaseInvoiceUrl)

                        .dueDay(request.getDueDay())
                        .dueMonth(request.getDueMonth())
                        .dueYear(request.getDueYear())

                        .createdAt(LocalDateTime.now())
                        .build();

                SupplierPayment supplierPayment1 = supplierPaymentRepository.save(supplierPayment);

                // Update inventory status and associate with supplier
                Supplier finalSupplier = supplier;
                inventoryList.forEach(inventory -> {
                    inventory.setStatus(true);
                    inventory.setInvoiceRefNumber(ref);
                    inventory.setSupplier(finalSupplier);
                    inventoryRepository.save(inventory);
                });

                // Submit expenses if payment day exists
                if (request.getPaymentDay() != null) {
                    ExpenditureRequest expenditureRequest = new ExpenditureRequest();
                    expenditureRequest.setExpenseName("Inventory Purchase From " + finalSupplier.getName());
                    expenditureRequest.setExpDay(request.getPaymentDay());
                    expenditureRequest.setExpMonth(request.getPaymentMonth());
                    expenditureRequest.setExpYear(request.getPaymentYear());
                    expenditureRequest.setAddedBy(user);
                    expenditureRequest.setAmountSpent(request.getAmountPaid());
                    expenditureService.modifyExpenseEntity(expenditureRequest, null);
                }

                //submit payment history
                addSupplierPaymentHistory(request, supplierPayment1);

                return ResponseEntity.ok().body(StandardResponse.success("Inventory Updated"));
            }

            return ResponseEntity.badRequest().body(StandardResponse.error("Invoice reference number already exists"));

        } catch (Exception e) {
            // Log the exception or handle it as needed
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StandardResponse.error("An error occurred while processing the request: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> updateSupplierToInventory(SupplierPaymentRequest request, Long supplierPaymentId) throws IOException {
        try {
            var user = userDetailsService.getAuthenticatedUser();

            if (user == null) {
                return ResponseEntity.ok().body(new Response("failed", "You are unauthorized"));
            }

            var intelRxId = userDetailsService.getIntelRxId();

            ResponseEntity<?> responseEntity = inventoryValidation.validateUpdateSupplierRequest(request);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                return responseEntity;
            }

            var supplierPayment = supplierPaymentRepository.findByIdAndIntelRxId
                    (supplierPaymentId, intelRxId).orElse(null);
            if (supplierPayment == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("supplierId not found"));
            }

            List<Inventory> optionalInventory = inventoryRepository.findAllByInvoiceRefNumber(supplierPayment.getInvoiceRefNumber());

            Inventory inv = null;
            String ref = "";
            if (optionalInventory.isEmpty()) {
                inv = optionalInventory.get(0);
                ref = inv.getInvoiceRefNumber();
            }

            var paymentStatus = inventoryValidation.checkPaymentStatusIfNotExists(request.getPaymentStatusId());
            if (paymentStatus == null) {
                return ResponseEntity.badRequest().body(StandardResponse.error("paymentStatusId can't Be Not Found"));
            }

            var paymentMethod = inventoryValidation.checkPaymentMethodIfNotExists(request.getPaymentMethodId());

            var medInvoice = supplierRepository.findByInvoiceRefNumber(ref);

            if (medInvoice.isEmpty()) {

                double balance = supplierPayment.getInvoiceAmount() - supplierPayment.getTotalAmountPaid();
                if (request.getAmountPaid() > balance) {
                    return ResponseEntity.badRequest().body(StandardResponse.error("your balance to be paid " + balance));
                }

                SupplierPayment newPayment = new SupplierPayment();
                newPayment.setIntelRxId(intelRxId);
                newPayment.setInvoiceRefNumber(supplierPayment.getInvoiceRefNumber());
                newPayment.setAmountPaid(request.getAmountPaid());
                newPayment.setPaymentMethod(paymentMethod);
                newPayment.setPaymentStatus(paymentStatus);
                newPayment.setSupplier(supplierPayment.getSupplier());
                newPayment.setPaymentDay(request.getPaymentDay());
                newPayment.setPaymentMonth(request.getPaymentMonth());
                newPayment.setPaymentYear(request.getPaymentYear());
                newPayment.setUser(user);
                if (request.getBankTransfer() != null) {
                    // Save the file to the upload directory
                    String bankTransfer = s3Service.uploadFileDoc(request.getBankTransfer(), "pharmacy");
                    newPayment.setBankTransfer(bankTransfer);
                }

                supplierPayment.setPaymentStatus(paymentStatus);
                supplierPayment.setBalanceDue(supplierPayment.getBalanceDue() - request.getAmountPaid());
                supplierPayment.setTotalAmountPaid(supplierPayment.getTotalAmountPaid() + request.getAmountPaid());

                if (request.getPurchaseInvoice() != null) {
                    // Save the file to the upload directory
                    String purchaseInvoice = s3Service.uploadFileDoc(request.getPurchaseInvoice(), "pharmacy");
                    supplierPayment.setPurchaseInvoice(purchaseInvoice);
                }

                supplierPayment.setCreatedAt(LocalDateTime.now());
                SupplierPayment supplierPayment1 = supplierPaymentRepository.save(supplierPayment);

                //submit payment history
                addSupplierPaymentHistory(request, supplierPayment1);

                //SUbmit Expenses
                ExpenditureRequest expenditureRequest = new ExpenditureRequest();
                expenditureRequest.setExpenseName("Inventory Purchase From " + request.getName());
                expenditureRequest.setExpDay(request.getPaymentDay());
                expenditureRequest.setExpMonth(request.getPaymentMonth());
                expenditureRequest.setExpYear(request.getPaymentYear());
                expenditureRequest.setAddedBy(user);
                expenditureRequest.setAmountSpent(request.getAmountPaid());
                expenditureRequest.setExpenditureType("Inventory");
                expenditureService.modifyExpenseEntity(expenditureRequest, null);

            }
            return ResponseEntity.ok().body(StandardResponse.success("Inventory Updated"));
        } catch (Exception e) {
            // Log the exception or handle it as needed
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StandardResponse.error("An error occurred while processing the request"));
        }
    }

    public ResponseEntity<?> supplierPayments(Long supplierId) {
        var intelRxId = userDetailsService.getIntelRxId();
        if (supplierId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("supplierId is required"));
        }

        var supplier = supplierRepository.findByIdAndIntelRxId(supplierId,
                intelRxId).orElse(null);

        if (supplier == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("supplierId not found"));
        }

        List<SupplierPayment> supplierPaymentList = supplierPaymentRepository.
                findAllBySupplier(supplier);

        if (supplierPaymentList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success("empty", supplierPaymentList));
        }

        List<SupplierResponse> responseList = supplierPaymentList.stream()
                .map(supplierMapping::mapToSupplierPaymentResponse).collect(Collectors.toList());


        return ResponseEntity.ok(StandardResponse.success(responseList));

    }

    public ResponseEntity<?> supplierPayment(String InvoiceRefNumber) {
        var intelRxId = userDetailsService.getIntelRxId();
        if (utility.isNullOrEmpty(InvoiceRefNumber)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("InvoiceRefNumber is required"));
        }

        var supplier = supplierPaymentRepository.findByInvoiceRefNumber(InvoiceRefNumber).orElse(null);

        if (supplier == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("InvoiceRefNumber not found"));
        }

        SupplierResponse responseList = supplierMapping.mapToSupplierPaymentResponse(supplier);
        return ResponseEntity.ok(StandardResponse.success(responseList));

    }

    public ResponseEntity<?> supplierPaymentInvoice(String InvoiceRefNumber, Pageable pageable) {
        var intelRxId = userDetailsService.getIntelRxId();
        if (utility.isNullOrEmpty(InvoiceRefNumber)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("InvoiceRefNumber is required"));
        }

        Optional<SupplierPayment> optional = supplierPaymentRepository.findByInvoiceRefNumber(InvoiceRefNumber);
        if (optional == null || optional.isEmpty()) {
            return ResponseEntity.badRequest().body(StandardResponse.error("InvoiceRefNumber not found"));
        }
        Page<Inventory> inventoryList = inventoryRepository.findAllByInvoiceRefNumberAndIntelRxId(InvoiceRefNumber, intelRxId, pageable);
        if (inventoryList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(inventoryList));
        }

        Page<InventoryResponse> orderResponseList = inventoryList.map(filterInventory::mapToAddedInventoryResponse);

        return ResponseEntity.ok(StandardResponse.success(orderResponseList));


    }

}
