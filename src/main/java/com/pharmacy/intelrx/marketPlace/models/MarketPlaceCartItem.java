package com.pharmacy.intelrx.marketPlace.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.*;
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
@Table(name = "market_place_cart_items")
public class MarketPlaceCartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String intelRxId;

    //@JsonIgnore
    @ManyToOne
    @JoinColumn(name = "inventory_id", referencedColumnName = "id")
    private Inventory inventory;

    private int quantity;

    private int vat;

    private double amount;

    private boolean status = false;

    private String salesStatus;//Placed,Confirmed,Processed,Pickup,Delivery,Completed


    //@JsonIgnore
    @ManyToOne
    @JoinColumn(name = "supplier_pharmacy_id", referencedColumnName = "id")
    private Pharmacy supplierPharmacy;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;//the current user buying

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private MarketPlaceOrder order;


    private LocalDateTime createdAt;

    public double calculateSalesProfitAmount() {
        double calculatedAmount = this.quantity * this.inventory.getCostPrice();
        return this.amount - calculatedAmount;
    }


}
