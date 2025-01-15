package com.pharmacy.intelrx.marketPlace.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.CartItem;
import com.pharmacy.intelrx.pharmacy.models.Patient;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
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
@Table(name = "market_place_orders")
public class MarketPlaceOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String intelRxId;

    private String orderRef;

    private double totalAmount;


    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;//the current user submitting order

    @ManyToOne
    @JoinColumn(name = "order_bill_info_id", referencedColumnName = "id")
    private OrderBillInfo orderBillInfo;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<MarketPlaceCartItem> cartItem;

    private boolean status = false;

    private String salesStatus;//Placed,Confirmed,Processed,Pickup,Delivery,Completed

    private String deliveryMethod;//Pickup,Delivery

    @Column(name = "delivery_date") // Make sure the column name matches the database column
    private LocalDateTime deliveryDate;

    @Column(name = "ordered_date") // Make sure the column name matches the database column
    private LocalDateTime orderedDate;



}
