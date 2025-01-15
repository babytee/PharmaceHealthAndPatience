package com.pharmacy.intelrx.pharmacy.dto.inventory;

import com.pharmacy.intelrx.auxilliary.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierPaymentRequest {

    private String name;

    private String phoneNumber;

    private String intelRxId;

    private User user;

    private String invoiceRefNumber;
    private Long supplierId;
    private Long paymentStatusId;
    private Long paymentMethodId;
    private Double invoiceAmount;
    private Double amountPaid;
    private Double balanceDue;

    private Integer dueDay;
    private Integer dueMonth;
    private Integer dueYear;

    private Integer paymentDay;
    private Integer paymentMonth;
    private Integer paymentYear;

    private String purchaseInvoice;
    private String bankTransfer;
}
