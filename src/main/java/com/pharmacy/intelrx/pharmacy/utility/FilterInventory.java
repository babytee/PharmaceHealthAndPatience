package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.auxilliary.models.PaymentMethod;
import com.pharmacy.intelrx.auxilliary.models.PaymentStatus;
import com.pharmacy.intelrx.auxilliary.services.S3Service;
import com.pharmacy.intelrx.brand.models.Brand;
import com.pharmacy.intelrx.brand.models.BrandClass;
import com.pharmacy.intelrx.brand.models.BrandForm;
import com.pharmacy.intelrx.brand.models.Size;
import com.pharmacy.intelrx.pharmacy.dto.inventory.*;
import com.pharmacy.intelrx.pharmacy.models.Inventory;
import com.pharmacy.intelrx.pharmacy.models.Supplier;
import com.pharmacy.intelrx.pharmacy.models.SupplierPayment;
import com.pharmacy.intelrx.pharmacy.models.TransferInventory;
import com.pharmacy.intelrx.pharmacy.models.orgSettings.DrugExpirationNotification;
import com.pharmacy.intelrx.pharmacy.models.orgSettings.OutOfStockNotification;
import com.pharmacy.intelrx.pharmacy.repositories.InventoryRepository;
import com.pharmacy.intelrx.pharmacy.repositories.SupplierPaymentRepository;
import com.pharmacy.intelrx.pharmacy.repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class FilterInventory {
    private final SupplierRepository supplierRepository;
    private final SupplierPaymentRepository supplierPaymentRepository;
    private final InventoryRepository inventoryRepository;
    private final S3Service s3Service;

    public VerifyInventoryResponse mapToVerifyInventoryRes(Inventory inventory) {
        VerifyInventoryResponse verifyInventoryResponse = new VerifyInventoryResponse();

        InventoryResponse inventoryResponse = mapToAddedInventoryResponse(inventory);
        verifyInventoryResponse.setInventory(inventoryResponse);

        return verifyInventoryResponse;
    }

    public InventoryResponse mapToAddedInventoryResponse(Inventory inventory) {

        InventoryResponse inventoryResponse = new InventoryResponse();
        inventoryResponse.setId(inventory.getId());
        inventoryResponse.setInventoryType(inventory.getInventoryType());
        inventoryResponse.setBarCodeNumber(inventory.getBarCodeNumber());

        String MedName = inventory.getItemName();

        if (inventory.getInventoryType().equals("inventory.getInventoryType()")
                || inventory.getInventoryType() == "inventory.getInventoryType()" ||
                Objects.equals(inventory.getInventoryType(), "MEDICATION")) {
            MedName = inventory.getBrand().getName() + " - " + inventory.getItemName();
        }


        inventoryResponse.setItemName(MedName);
        inventoryResponse.setGenericName(inventory.getItemName());

        inventoryResponse.setIntelRxId((inventory.getIntelRxId()));

        BrandReqRes brandReqRes = mapToBrand(inventory.getBrand());
        inventoryResponse.setBrand(brandReqRes);

        BrandFormReqRes brandFormReqRes = mapToBrandForm(inventory.getBrandForm());
        inventoryResponse.setBrandForm(brandFormReqRes);

        BrandClassReqRes brandClassReqRes = mapToBrandClass(inventory.getBrandClass());
        inventoryResponse.setBrandClass(brandClassReqRes);

        if (inventory.getSize() != null) {
            SizeReqRes sizeReqRes = mapToMedicationSize(inventory.getSize());
            inventoryResponse.setMedicationSize(sizeReqRes);
        } else {
            inventoryResponse.setMedicationSize(null);
        }

        inventoryResponse.setWholeSalePrice(inventory.getWholeSalePrice());
        inventoryResponse.setWholeSaleQuantity(inventory.getWholeSaleQuantity());

        inventoryResponse.setQuantity(inventory.getQuantity());
        inventoryResponse.setCostPrice(inventory.getCostPrice());
        inventoryResponse.setSalePrice(inventory.getSalePrice());
        inventoryResponse.setExpDay(inventory.getExpDay());
        inventoryResponse.setExpMonth(inventory.getExpMonth());
        inventoryResponse.setExpYear(inventory.getExpYear());

        inventoryResponse.setPoison(inventory.isPoison());
        inventoryResponse.setCategory(inventory.isPoison() ? "Poison" : "Safe");

        return inventoryResponse;
    }

    public InventoryResponse mapToTransferInventoryResponse(TransferInventory transferInventory) {
        Inventory inventory = transferInventory.getInventory();

        InventoryResponse inventoryResponse = new InventoryResponse();
        inventoryResponse.setId(inventory.getId());
        inventoryResponse.setInventoryType(inventory.getInventoryType());
        inventoryResponse.setBarCodeNumber(inventory.getBarCodeNumber());
        inventoryResponse.setItemName(inventory.getItemName());
        inventoryResponse.setGenericName(inventory.getItemName());
        inventoryResponse.setIntelRxId((inventory.getIntelRxId()));

        BrandReqRes brandReqRes = mapToBrand(inventory.getBrand());
        inventoryResponse.setBrand(brandReqRes);

        BrandFormReqRes brandFormReqRes = mapToBrandForm(inventory.getBrandForm());
        inventoryResponse.setBrandForm(brandFormReqRes);

        BrandClassReqRes brandClassReqRes = mapToBrandClass(inventory.getBrandClass());
        inventoryResponse.setBrandClass(brandClassReqRes);

        SizeReqRes sizeReqRes = mapToMedicationSize(inventory.getSize());
        inventoryResponse.setMedicationSize(sizeReqRes);

        inventoryResponse.setWholeSalePrice(inventory.getWholeSalePrice());
//        inventoryResponse.setWholeSaleQuantity(inventory.getWholeSaleQuantity());

        inventoryResponse.setQuantity(transferInventory.getQuantity());
        inventoryResponse.setCostPrice(inventory.getCostPrice());
        inventoryResponse.setSalePrice(inventory.getSalePrice());
        inventoryResponse.setExpDay(inventory.getExpDay());
        inventoryResponse.setExpMonth(inventory.getExpMonth());
        inventoryResponse.setExpYear(inventory.getExpYear());


        inventoryResponse.setPoison(inventory.isPoison());

        return inventoryResponse;
    }


    public OutOfStockResponse mapToOutOfStockResponse(Inventory inventory, OutOfStockNotification outOfStockNotification) {
        OutOfStockResponse outOfStockResponse = new OutOfStockResponse();
        int stockMeasure = 0;

        if (inventory.getInventoryType().equals("MEDICATION")) {
            stockMeasure = outOfStockNotification.getMedication();
        } else if (inventory.getInventoryType().equals("GROCERY")) {
            stockMeasure = outOfStockNotification.getGrocery();
        }

        int quantity = inventory.getQuantity();
        if (stockMeasure >= quantity) {
            String stockLeft = quantity + " " + inventory.getSize().getName() + " left";
            String itemName = inventory.getItemName().toUpperCase();

            SupplierPayment supplier = supplierPaymentRepository.findByInvoiceRefNumber(inventory.getInvoiceRefNumber())
                    .orElse(null);
            if (supplier != null) {
                LocalDateTime createdAt = supplier.getCreatedAt();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM, yyyy");
                String formattedDate = createdAt.format(formatter);

                String brandName = inventory.getBrand().getName().toUpperCase();

                String invName = itemName + "(" + brandName + ")";

                String description = invName + " which you bought on " + formattedDate +
                        " from " + supplier.getSupplier().getName() + ", is out of stock.";

                outOfStockResponse.setInventoryId(inventory.getId());
                outOfStockResponse.setStockLeft(stockLeft);
                outOfStockResponse.setDescription(description);
                outOfStockResponse.setInventoryDetails(mapToAddedInventoryResponse(inventory));

            } else {
                System.out.println("Supplier not found for invoiceRefNumber: " + inventory.getInvoiceRefNumber());
            }
        } else {
            System.out.println("Stock measure is less than quantity for inventory ID: " + inventory.getId());
        }

        return outOfStockResponse;
    }

    public OutOfStockResponse mapToDrugExpirationResponse(Inventory inventory, DrugExpirationNotification drugExpirationNotification) {
        OutOfStockResponse outOfStockResponse = new OutOfStockResponse();
        String stockMeasure = drugExpirationNotification.getFrequency().getName();
        String stockLeft = inventory.getQuantity() + " " + inventory.getSize().getName() + " left";

        // Validate and create the date
        LocalDate targetDate;
        try {
            targetDate = LocalDate.of(inventory.getExpYear(), inventory.getExpMonth(), inventory.getExpDay());
        } catch (DateTimeException e) {
            System.out.println("Invalid expiration date for inventory ID: " + inventory.getId());
            return null;  // or you can return an empty OutOfStockResponse or handle as needed
        }

        // Calculate the expiration date
        String expDate = calculateExpirationDate(stockMeasure, targetDate, inventory);

        if (expDate != null) {
            outOfStockResponse.setInventoryId(inventory.getId());
            outOfStockResponse.setStockLeft(stockLeft);
            outOfStockResponse.setDescription(expDate);
            outOfStockResponse.setInventoryDetails(mapToAddedInventoryResponse(inventory));
        } else {
            System.out.println("Expiration date is not close for inventory ID: " + inventory.getId());
        }
        return outOfStockResponse;
    }

    private String calculateExpirationDate(String duration, LocalDate targetDate, Inventory inventory) {
        if (inventory != null && inventory.getId() == null) {
            return null;
        }

        // Check if brand is null
        if (inventory.getBrand() == null) {
            System.out.println("Brand is null for inventory ID: " + inventory.getId());
            return null; // Handle this case appropriately; could return a default message or null
        }

        String brandName = inventory.getBrand().getName().toUpperCase();
        String itemName = inventory.getItemName().toUpperCase();
        String invName = itemName + "(" + brandName + ")";

        // Set a dynamic duration (e.g., 3 weeks, 5 months, 4 days)
        Period dynamicDuration = parseDynamicDuration(duration);  // You can change this to Period.ofMonths(5), Period.ofDays(4), etc.

        // Calculate the current date
        LocalDate currentDate = LocalDate.now();

        // Calculate the expiration date based on the dynamic duration
        LocalDate expirationDate = targetDate.minus(dynamicDuration);
        String daysLeft = null;

        // Check if the current date is within the dynamic duration to expiration
        if (currentDate.isBefore(targetDate) && currentDate.isAfter(expirationDate)) {
            daysLeft = invName + " will be expiring in " + ChronoUnit.DAYS.between(currentDate, targetDate) + " days. You are advised to carryout a physical check to confirm expiration dates to ensure customer/patient safety";
            System.out.println("Within dynamic duration to expiration. Days left: " + ChronoUnit.DAYS.between(currentDate, targetDate));
        } else if (currentDate.isEqual(targetDate)) {
            daysLeft = invName + " will expire today. You are advised to carryout a physical check to confirm expiration dates to ensure customer/patient safety";
            System.out.println("Today is the expiration date.");
        } else if (currentDate.isAfter(targetDate)) {
            daysLeft = invName + " has expired. You are advised to carryout a physical check to confirm expiration dates to ensure customer/patient safety";
            System.out.println("The date has expired.");
        } else {
            daysLeft = invName + " will be expiring in " + ChronoUnit.DAYS.between(currentDate, targetDate) + " days. You are advised to carryout a physical check to confirm expiration dates to ensure customer/patient safety";
            System.out.println("Outside dynamic duration to expiration. Days left: " + ChronoUnit.DAYS.between(currentDate, targetDate));
        }
        return daysLeft;
    }

    private Period parseDynamicDuration(String durationString) {
        String[] parts = durationString.split("\\s+");
        int amount = Integer.parseInt(parts[0]);
        String unit = parts[1].toLowerCase(); // assuming the unit is always in lowercase

        switch (unit) {
            case "week":
            case "weeks":
            case "Week":
            case "Weeks":
                return Period.ofWeeks(amount);
            case "month":
            case "months":
            case "Month":
            case "Months":
                return Period.ofMonths(amount);
            case "day":
            case "days":
            case "Day":
            case "Days":
                return Period.ofDays(amount);
            // Add more cases for other units as needed
            default:
                throw new IllegalArgumentException("Invalid duration unit: " + unit);
        }
    }


    public BrandReqRes mapToBrand(Brand brand) {
        BrandReqRes brandReqRes = new BrandReqRes();
        if (brand != null) {
            brandReqRes.setId(brand.getId());
            brandReqRes.setName(brand.getName());
        }
        return brandReqRes;
    }


    public BrandClassReqRes mapToBrandClass(BrandClass brandClass) {
        BrandClassReqRes brandClassReqRes = new BrandClassReqRes();
        if (brandClass != null) {
            brandClassReqRes.setId(brandClass.getId());
            brandClassReqRes.setName(brandClass.getName());
        }

        return brandClassReqRes;
    }

    public BrandFormReqRes mapToBrandForm(BrandForm brandForm) {

        BrandFormReqRes brandFormReqRes = new BrandFormReqRes();
        if (brandForm != null) {
            brandFormReqRes.setId(brandForm.getId());
            brandFormReqRes.setName(brandForm.getName());
        }

        return brandFormReqRes;
    }

    public SizeReqRes mapToMedicationSize(Size size) {
        SizeReqRes sizeReqRes = new SizeReqRes();
        sizeReqRes.setId(size.getId());
        sizeReqRes.setName(size.getName());

        return sizeReqRes;
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

        List<SupplierPayment> supplierPayments = supplierPaymentRepository.findAllBySupplier(supplierPayment.getSupplier());

        supplierResponse.setNoOfPayment(supplierPayments.size());

        supplierResponse.setDueYear(supplierPayment.getDueYear());
        supplierResponse.setDueMonth(supplierPayment.getDueMonth());
        supplierResponse.setDueDay(supplierPayment.getDueDay());

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
            sum += inventory.getCostPrice();
        }

        supplierResponse.setTotalCost(sum);

        supplierResponse.setInvoiceAmount(supplierPayment.getInvoiceAmount());

        return supplierResponse;
    }


    public PaymentStatusReqRes mapToPaymentStatus(PaymentStatus paymentStatus) {
        PaymentStatusReqRes paymentStatusReqRes = new PaymentStatusReqRes();
        paymentStatusReqRes.setId(paymentStatus.getId());
        paymentStatusReqRes.setName(paymentStatus.getName());

        return paymentStatusReqRes;
    }

    public PaymentMethodReqRes mapToPaymentMethod(PaymentMethod paymentMethod) {
        PaymentMethodReqRes paymentMethodReqRes = new PaymentMethodReqRes();
        paymentMethodReqRes.setId(paymentMethod.getId());
        paymentMethodReqRes.setName(paymentMethod.getName());

        return paymentMethodReqRes;
    }


}
