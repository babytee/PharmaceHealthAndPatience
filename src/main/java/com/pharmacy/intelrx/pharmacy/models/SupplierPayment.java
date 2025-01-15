package com.pharmacy.intelrx.pharmacy.models;

import com.pharmacy.intelrx.auxilliary.models.PaymentMethod;
import com.pharmacy.intelrx.auxilliary.models.PaymentStatus;
import com.pharmacy.intelrx.auxilliary.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "supplier_payment")
public class SupplierPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String invoiceRefNumber;
    private String intelRxId;

    @ManyToOne
    @JoinColumn(name = "supplier_id", referencedColumnName = "id")
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "payment_status_id", referencedColumnName = "id")
    private PaymentStatus paymentStatus;

    @ManyToOne
    @JoinColumn(name = "payment_method_id", referencedColumnName = "id")
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "payment_day", columnDefinition = "integer default 0")
    private Integer paymentDay;

    @Column(name = "payment_month", columnDefinition = "integer default 0")
    private Integer paymentMonth;

    @Column(name = "payment_year", columnDefinition = "integer default 0")
    private Integer paymentYear;

    private String bankTransfer;

    private double amountPaid;

    private double invoiceAmount;

    private double balanceDue;

    private double totalAmountPaid;

    private String purchaseInvoice;

    @Column(name = "due_day", columnDefinition = "integer default 0")
    private Integer dueDay;

    @Column(name = "due_month", columnDefinition = "integer default 0")
    private Integer dueMonth;

    @Column(name = "due_year", columnDefinition = "integer default 0")
    private Integer dueYear;

    private LocalDateTime createdAt;
}
