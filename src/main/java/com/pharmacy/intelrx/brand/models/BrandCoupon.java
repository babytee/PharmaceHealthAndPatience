package com.pharmacy.intelrx.brand.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "brand_coupons")
public class BrandCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productCouponType; //General or Specific

    @ManyToOne
    private PharmaceuticalBrand pharmaceuticalBrand;




    private LocalDateTime createdAt;
}
