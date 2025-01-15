package com.pharmacy.intelrx.generalCoupon.repositories;

import com.pharmacy.intelrx.generalCoupon.models.CouponTransactionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CouponTransactionHistoryRepository extends JpaRepository<CouponTransactionHistory,Long>{
    @Query("SELECT c FROM CouponTransactionHistory c " +
            "WHERE c.intelRxId = :intelRxId " +
            "AND (:dateFrom IS NULL OR c.createdAt >= :dateFrom) " +
            "AND (:dateTo IS NULL OR c.createdAt <= :dateTo) " +
            "AND (:trxCouponType IS NULL OR c.trxCouponType = :trxCouponType) " +
            "AND (:trxType IS NULL OR c.trxType = :trxType) " +
            "AND (" +
            ":searchTerm IS NULL OR " +
            "LOWER(c.trxRef) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.trxStatus) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.trxCouponType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "CAST(c.amount AS text) LIKE CONCAT('%', :searchTerm, '%')" +
            ")")
    Page<CouponTransactionHistory> findByDateRangeTrxCouponTypeAndSearchTerm(
            @Param("intelRxId") String intelRxId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("trxCouponType") String trxCouponType,
            @Param("trxType") String trxType,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );


    @Query("SELECT c FROM CouponTransactionHistory c " +
            "WHERE c.intelRxId = :intelRxId")
    Page<CouponTransactionHistory> findByIntelRxId(
            @Param("intelRxId") String intelRxId,
            Pageable pageable
    );


}
