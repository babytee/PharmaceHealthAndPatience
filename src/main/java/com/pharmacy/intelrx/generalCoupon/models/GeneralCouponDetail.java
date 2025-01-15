package com.pharmacy.intelrx.generalCoupon.models;

import com.pharmacy.intelrx.admin.models.Admin;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "general_coupon_details")
public class GeneralCouponDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String couponCode;

    private String stateVicinity;// All States or Some Specific State

    @ManyToOne
    private Pharmacy pharmacyVicinity;

    @ManyToOne
    private PharmacyBranch branch;

    private String couponDetailStatus; //active,expired,disabled

    @ManyToOne
    private GeneralCoupon generalCoupon;

    @ManyToOne
    private Order orderReference;

    @ManyToOne
    private Admin createdByAdmin;

    @ManyToOne
    private Pharmacy createdByPharmacy;


    private LocalDate dateUsed;
    private LocalTime timeUsed;

}
