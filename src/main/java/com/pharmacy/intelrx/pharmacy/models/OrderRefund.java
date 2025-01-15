package com.pharmacy.intelrx.pharmacy.models;

import com.pharmacy.intelrx.auxilliary.models.PaymentMethod;
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
@Table(name = "order_refund")
public class OrderRefund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String intelRxId;

    @Column(name = "reason_for_refund")
    private String reasonForRefund;

    @Column(name = "optional_reason")
    private String optionalReason;

    @Column(name = "refund_amount")
    private double refundAmount;

    @ManyToOne
    @JoinColumn(name = "refunded_by", referencedColumnName = "id")
    private User refundedBy;

    @ManyToOne
    @JoinColumn(name = "refund_method", referencedColumnName = "id")
    private PaymentMethod refundMethod;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "pharmacy_branch_id",referencedColumnName = "id")
    private PharmacyBranch pharmacyBranch;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;


}
