package com.pharmacy.intelrx.generalCoupon.models;

import com.pharmacy.intelrx.admin.models.Admin;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "general_coupons")
public class GeneralCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String couponCode;
    private String couponTitle;
    private String couponEventType;//General,Sponsored,Event
    private String couponDescription;

    private LocalDate startDate;
    private LocalDate endDate;
    private double couponValue;

    private String sharingCapacity; //automatic or manual
    private String couponVicinity; //State or Pharmacy

    private double amountPerPerson;
    private int countPerPerson;

    //this works for sharingCapacity when it is automatic
    private String coverage;// All States or Some Specific State

    private String couponStatus; //active,expired,disabled

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type")
    private CouponType couponType;//PHARMACY,GENERAL,BRAND

    @ManyToOne
    private Admin createdByAdmin;

    @ManyToOne
    private Pharmacy createdByPharmacy;

    @OneToMany
    private List<GeneralCouponDetail> generalCouponDetails;


    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // This method will automatically generate a couponCode before the entity is persisted
    @PrePersist
    public void generateCouponCode() {
        if (couponCode == null || couponCode.isEmpty()) {
            // Generate a unique coupon code using UUID and strip non-alphanumeric characters
            this.couponCode = "CPN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

}
