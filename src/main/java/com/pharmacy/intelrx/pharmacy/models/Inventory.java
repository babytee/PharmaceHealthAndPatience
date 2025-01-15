package com.pharmacy.intelrx.pharmacy.models;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.brand.models.Brand;
import com.pharmacy.intelrx.brand.models.BrandClass;
import com.pharmacy.intelrx.brand.models.BrandForm;
import com.pharmacy.intelrx.brand.models.Size;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "inventories")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String intelRxId;

    private String inventoryType;//MEDICATION or GROCERY
    private String barCodeNumber;
    private String itemName;

    @ManyToOne
    @JoinColumn(name = "brand_id", referencedColumnName = "id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "brand_class_id", referencedColumnName = "id")
    private BrandClass brandClass;

    @ManyToOne
    @JoinColumn(name = "brand_form_id", referencedColumnName = "id")
    private BrandForm brandForm;

    @ManyToOne
    @JoinColumn(name = "size_id", referencedColumnName = "id")
    private Size size;

    @ManyToOne
    @JoinColumn(name = "supplier_id", referencedColumnName = "id")
    private Supplier supplier;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "cost_price")
    private double costPrice;

    @Column(name = "sale_price")
    private double salePrice;

    @Column(name = "whole_sale_price")
    private double wholeSalePrice;

    @Column(name = "whole_sale_quantity")
    private int wholeSaleQuantity;

    @Column(name = "exp_day")
    private int expDay;

    @Column(name = "exp_month")
    private int expMonth;

    @Column(name = "exp_year")
    private int expYear;

    private boolean status = false;

    @Column(name = "delete_status")
    private Boolean deleteStatus;

    private boolean poison = false;

    private String invoiceRefNumber;

    @Column(name = "bank_transfer_receipt", columnDefinition = "TEXT")
    private String bankTransferReceipt;

    @Column(name = "purchase_invoice", columnDefinition = "TEXT")
    private String purchaseInvoice;

    @ManyToOne
    @JoinColumn(name = "pharmacy_branch_id",referencedColumnName = "id")
    private PharmacyBranch pharmacyBranch;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;


}
