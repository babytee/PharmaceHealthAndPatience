package com.pharmacy.intelrx.generalCoupon.models;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coupon_transaction_histories")
public class CouponTransactionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String intelRxId;
    private String trxRef;
    private String description;
    private Double amount;
    private String trxStatus;
    private String trxType;//debit or credit
    private String trxCouponType;//BRAND, GENERAL, IN_HOUSE(Pharmacy)
    private String couponCode;

    @ManyToOne
    private User trxBy;//the user who perform the transaction

    @ManyToOne
    private Order order;


    private LocalDateTime createdAt;

    @PrePersist
    public void generateCouponCode() {
        if (trxRef == null || trxRef.isEmpty()) {
            // Generate a unique coupon code using UUID and strip non-alphanumeric characters
            this.trxRef = "trxRef" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}
