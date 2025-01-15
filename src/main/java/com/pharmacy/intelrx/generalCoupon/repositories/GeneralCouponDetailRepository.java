package com.pharmacy.intelrx.generalCoupon.repositories;

import com.pharmacy.intelrx.generalCoupon.models.GeneralCoupon;
import com.pharmacy.intelrx.generalCoupon.models.GeneralCouponDetail;
import com.pharmacy.intelrx.pharmacy.models.Order;
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
public interface GeneralCouponDetailRepository extends JpaRepository<GeneralCouponDetail, Long> {    // Find all GeneralCouponDetail by GeneralCoupon and Order

    List<GeneralCouponDetail> findByGeneralCouponAndOrderReference(GeneralCoupon generalCoupon, Order order);

    List<GeneralCouponDetail> findByGeneralCoupon(GeneralCoupon generalCoupon);

    @Query("SELECT COUNT(g) FROM GeneralCouponDetail g WHERE g.generalCoupon = :generalCoupon AND g.orderReference IS NOT NULL")
    int countByGeneralCouponAndOrderNotNull(@Param("generalCoupon") GeneralCoupon generalCoupon);

    @Query("SELECT COUNT(g) FROM GeneralCouponDetail g WHERE g.generalCoupon = :generalCoupon AND g.orderReference IS NOT NULL AND g.generalCoupon.couponType <> 'IN_HOUSE'")
    int countByGeneralCouponAndOrderNotNullAndNotInHouse(@Param("generalCoupon") GeneralCoupon generalCoupon);


    @Query("SELECT COUNT(g) FROM GeneralCouponDetail g WHERE g.generalCoupon = :generalCoupon AND g.orderReference IS NULL")
    int countByGeneralCouponAndOrderNull(@Param("generalCoupon") GeneralCoupon generalCoupon);

    @Query("SELECT g FROM GeneralCouponDetail g WHERE " +
            "LOWER(g.couponCode) LIKE LOWER(CONCAT('%', :couponCode, '%'))")
    Page<GeneralCouponDetail> findByCouponCode(
            @Param("couponCode") String couponCode,
            Pageable pageable);

    Optional<GeneralCouponDetail> findByCouponCodeAndOrderReferenceId(String couponCode, Long orderId);

    Optional<GeneralCouponDetail> findByPharmacyVicinity(Pharmacy pharmacy);

    Optional<GeneralCouponDetail> findFirstByPharmacyVicinity(Pharmacy pharmacy);


    @Query(value = "SELECT * FROM general_coupon_details WHERE coupon_code = :couponCode AND order_reference_id IS NULL LIMIT 1", nativeQuery = true)
    Optional<GeneralCouponDetail> findFirstByCouponCodeAndOrderReferenceIsNull(@Param("couponCode") String couponCode);


}
