package com.pharmacy.intelrx.pharmacy.models;

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
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String intelRxId;

    private String orderRef;

    private double totalAmount;

    private String couponCode;

    private double couponAmount;

    private double cashPayment;

    private double transferPayment;

    private double posPayment;

    private double totalPayable;

    private double balance;

    private double changeReturn;

    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "pharmacy_branch_id",referencedColumnName = "id")
    private PharmacyBranch pharmacyBranch;

    @ManyToOne
    @JoinColumn(name = "sales_person_id", referencedColumnName = "id")
    private User salesPerson;

    @ManyToOne
    @JoinColumn(name = "cashier_id", referencedColumnName = "id")
    private User cashier;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<CartItem> cartItem;

    private boolean status = false;

    private String salesStatus;//Completed, Refunded

    @Column(name = "ordered_date") // Make sure the column name matches the database column
    private LocalDateTime orderedDate;



}
