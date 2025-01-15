package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.pharmacy.dto.inventory.SupplierResponse;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.models.Supplier;
import com.pharmacy.intelrx.pharmacy.models.SupplierPayment;
import com.pharmacy.intelrx.pharmacy.models.SupplierPaymentHistory;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.SupplierPaymentHistoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.SupplierPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class SupplierMapping {
    private final InventoryRepository inventoryRepository;
    private final S3Service s3Service;
    private final SupplierPaymentRepository supplierPaymentRepository;
    private final SupplierPaymentHistoryRepository historyRepository;


    public SupplierResponse mapToSupplierResponse(Supplier supplier) throws IOException {
        SupplierResponse supplierResponse = new SupplierResponse();
        supplierResponse.setId(supplier.getId());
        supplierResponse.setName(supplier.getName());
        supplierResponse.setPhoneNumber(supplier.getPhoneNumber());

        List<Inventory> medInventoryList = inventoryRepository.
                findAllByInventoryTypeAndSupplier("MEDICATION", supplier);

        List<Inventory> gorceryInventoryList = inventoryRepository.
                findAllByInventoryTypeAndSupplier("GROCERY", supplier);

        supplierResponse.setMedication(medInventoryList.size());

        supplierResponse.setGrocery(gorceryInventoryList.size());

        return supplierResponse;
    }

    public SupplierResponse mapToSupplierPaymentResponse(SupplierPayment supplierPayment) {
        SupplierResponse supplierResponse = new SupplierResponse();
        supplierResponse.setId(supplierPayment.getId());
        supplierResponse.setName(supplierPayment.getSupplier().getName());
        supplierResponse.setPurchaseDate(supplierPayment.getCreatedAt());
        supplierResponse.setInvoiceRefNumber(supplierPayment.getInvoiceRefNumber());
        supplierResponse.setAmountPaid(supplierPayment.getAmountPaid());

        List<Inventory> medInventoryList = inventoryRepository.
                findAllByInventoryTypeAndInvoiceRefNumber("MEDICATION", supplierPayment.getInvoiceRefNumber());

        List<Inventory> gorceryInventoryList = inventoryRepository.
                findAllByInventoryTypeAndInvoiceRefNumber("GROCERY", supplierPayment.getInvoiceRefNumber());

        supplierResponse.setMedication(medInventoryList.size());

        supplierResponse.setGrocery(gorceryInventoryList.size());

        String bankTransfer = "";
        if (supplierPayment.getBankTransfer() != null) {
            S3Service.FetchedImage fetchedBankTransfer = s3Service.fetchImage(supplierPayment.getBankTransfer()); // Replace "your_image_name.jpg" with the actual image name
            if (fetchedBankTransfer != null) {
                bankTransfer = fetchedBankTransfer.getImageUrl();
            }
        }
        supplierResponse.setBankTransfer(bankTransfer);

        supplierResponse.setInvoiceAmount(supplierPayment.getInvoiceAmount());
        supplierResponse.setAmountPaid(supplierPayment.getTotalAmountPaid());
        supplierResponse.setBalanceDue(supplierPayment.getBalanceDue());

        String purchaseInvoice = "";
        if (supplierPayment.getPurchaseInvoice() != null) {
            S3Service.FetchedImage fetchedPurchaseInvoice = s3Service.fetchImage(supplierPayment.getPurchaseInvoice()); // Replace "your_image_name.jpg" with the actual image name
            if (fetchedPurchaseInvoice != null) {
                purchaseInvoice = fetchedPurchaseInvoice.getImageUrl();
            }
        }
        supplierResponse.setPurchaseInvoice(purchaseInvoice);

        List<SupplierPaymentHistory> supplierPaymentHistory = historyRepository.findByIntelRxIdAndSupplierPayment(
                supplierPayment.getIntelRxId(), supplierPayment);

        supplierResponse.setNoOfPayment(supplierPaymentHistory.size());

        supplierResponse.setDueYear(supplierPayment.getDueYear());
        supplierResponse.setDueMonth(supplierPayment.getDueMonth());
        supplierResponse.setDueDay(supplierPayment.getDueDay());
        String paymethod = "";
        for (SupplierPaymentHistory supplierPaymentHistory1 : supplierPaymentHistory) {
            paymethod += supplierPaymentHistory1.getPaymentMethod().getName() + " | ";
            supplierResponse.setPaymentMethods(paymethod);
        }
        supplierResponse.setPaymentStatus(supplierPayment.getPaymentStatus() != null ? supplierPayment.getPaymentStatus().getName() : null);
        supplierResponse.setPaymentMethod(supplierPayment.getPaymentMethod() != null ? supplierPayment.getPaymentMethod().getName() : null);

        supplierResponse.setDueYear(supplierPayment.getPaymentYear());
        supplierResponse.setDueMonth(supplierPayment.getPaymentMonth());
        supplierResponse.setDueDay(supplierPayment.getPaymentDay());

        supplierResponse.setPaymentStatus(supplierPayment.getPaymentStatus());

        List<Inventory> inventoryList = inventoryRepository.findAllByInvoiceRefNumber
                (supplierPayment.getInvoiceRefNumber());
        int sum = 0;
        for (Inventory inventory : inventoryList) {
            sum += inventory.getQuantity() * inventory.getCostPrice();
        }

        supplierResponse.setTotalCost(sum);

        supplierResponse.setInvoiceAmount(supplierPayment.getInvoiceAmount());

        return supplierResponse;
    }


}
