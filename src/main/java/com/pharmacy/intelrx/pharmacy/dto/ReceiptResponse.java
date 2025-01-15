package com.pharmacy.intelrx.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceiptResponse {
    private String pharmacyName;
    private String branchName;
    private String pharmacyNumber;
    private Object branchAddress;
    private Object address;
    private Object soldTo;
    private Object cashier;
    private Object salesPerson;
    private String receiptNo;
    private Object cartOrder;
    private double total;
    private double vat;
    private double discount;
    private double invoiceAmount;
    private double cashTendered;
    private double posAmount;
    private double transferAmount;
    private String couponCode;
    private double couponAmount;
    private double changeReturn;
}
