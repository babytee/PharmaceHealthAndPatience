package com.pharmacy.intelrx.pharmacy.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacy.intelrx.auxilliary.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String intelRxId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "inventory_id", referencedColumnName = "id")
    private Inventory inventory;

    private int quantity;

    private int vat;

    private double amount;

    private boolean status = false;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private Patient patient;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "pharmacy_branch_id", referencedColumnName = "id")
    private PharmacyBranch pharmacyBranch;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;//the current user selling

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    @OneToOne(mappedBy = "cartItem", cascade = CascadeType.ALL)
    private MedPrescription medPrescription;

    private LocalDateTime createdAt;

    public double calculateSalesProfitAmount() {
        double calculatedAmount = this.quantity * this.inventory.getCostPrice();
        return this.amount - calculatedAmount;
    }


}
