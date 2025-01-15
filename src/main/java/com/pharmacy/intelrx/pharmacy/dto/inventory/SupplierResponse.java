package com.pharmacy.intelrx.pharmacy.dto.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupplierResponse {

    private Long id;
    private String name;

    private String phoneNumber;

    private String invoiceRefNumber;
    private Object paymentStatus;
    private Object paymentMethod;
    private String paymentMethods;
    private Double amountPaid;
    private Double balanceDue;

    private Integer dueDay;
    private Integer dueMonth;
    private Integer dueYear;


    private int medication;
    private int grocery;

    private String purchaseInvoice;
    private String bankTransfer;

    private double totalCost;

    private int  noOfPayment;


    private LocalDateTime purchaseDate;

    private double invoiceAmount;
}
