package com.pharmacy.intelrx.generalCoupon.repositories;

import com.pharmacy.intelrx.generalCoupon.models.CouponType;
import com.pharmacy.intelrx.generalCoupon.models.GeneralCoupon;
import com.pharmacy.intelrx.pharmacy.models.Pharmacy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneralCouponRepository extends JpaRepository<GeneralCoupon, Long> {
    // Fetch coupons with optional search by status, name, or code, sorted by creation date or value
    @Query("SELECT g FROM GeneralCoupon g " +
            "WHERE (:createdByPharmacy IS NULL OR g.createdByPharmacy = :createdByPharmacy) " +
            "AND (:couponStatus IS NULL OR g.couponStatus = :couponStatus) " +
            "AND (:searchTerm IS NULL OR LOWER(g.couponTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(g.couponCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'recentlyAdded' THEN g.createdAt END DESC, " +
            "CASE WHEN :sortBy = 'highestValue' THEN g.couponValue END DESC")
    Page<GeneralCoupon> findByFilter(
            @Param("createdByPharmacy") Pharmacy createdByPharmacy,
            @Param("couponStatus") String couponStatus,
            @Param("searchTerm") String searchTerm,
            @Param("sortBy") String sortBy,
            Pageable pageable);

    List<GeneralCoupon> findByCouponStatus(String couponStatus);

    Optional<GeneralCoupon> findByCouponCodeAndCouponType(String couponCode, CouponType couponType);
}
