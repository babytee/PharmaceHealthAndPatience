package com.pharmacy.intelrx.pharmacy.dto;

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
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private Long id;

    private String intelRxId;

    private String orderRef;

    private double totalAmount;

    private double changeReturn;

    private double cashPayment;

    private double transferPayment;

    private double posPayment;

    private double totalPayable;

    private double balance;

    private Object patient;

    private Object branch;

    private Object PaymentMethod;

    private Object salesPerson;

    private Object cashier;

    private Object orderRefund;

    private String salesStatus;

    private boolean status;

    private int poison;

    private LocalDateTime orderedDate;
}
