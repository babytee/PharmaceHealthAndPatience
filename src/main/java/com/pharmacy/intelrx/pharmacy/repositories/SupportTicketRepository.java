package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.CartItem;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.support.SupportTicket;
import com.pharmacy.intelrx.pharmacy.models.support.SupportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    // 1. Count total tickets, filtered by intelRxId if provided
    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE (:intelRxId IS NULL OR t.intelRxId = :intelRxId)")
    long countTotalTickets(@Param("intelRxId") String intelRxId);

    // 2. Count pending tickets, filtered by intelRxId if provided
    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE (:intelRxId IS NULL OR t.intelRxId = :intelRxId) " +
            "AND (t.ticketStatus = 'Pending' OR t.ticketStatus IS NULL)")
    long countPendingTickets(@Param("intelRxId") String intelRxId);

    // 3. Count resolved tickets, filtered by intelRxId if provided
    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE (:intelRxId IS NULL OR t.intelRxId = :intelRxId) " +
            "AND t.ticketStatus = 'Resolved'")
    long countResolvedTickets(@Param("intelRxId") String intelRxId);


    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.supportType = :supportType " +
            "AND (:intelRxId IS NULL OR t.intelRxId = :intelRxId) " +
            "AND EXTRACT(YEAR FROM t.createdAt) = :year " +
            "AND EXTRACT(MONTH FROM t.createdAt) = :month")
    long countBySupportTypeAndIntelRxIdAndCreatedAtYearAndCreatedAtMonth(
            @Param("supportType") SupportType supportType,
            @Param("intelRxId") String intelRxId,
            @Param("year") int year,
            @Param("month") int month);


    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.supportType = :supportType " +
            "AND (:intelRxId IS NULL OR t.intelRxId = :intelRxId) " +
            "AND t.createdAt BETWEEN :startDate AND :endDate")
    long countBySupportTypeAndIntelRxIdAndCreatedAtBetween(
            @Param("supportType") SupportType supportType,
            @Param("intelRxId") String intelRxId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.supportType = :supportType " +
            "AND (:intelRxId IS NULL OR t.intelRxId = :intelRxId) " +
            "AND t.createdAt >= CURRENT_DATE - 30")
    long countBySupportTypeAndIntelRxIdLast30Days(
            @Param("supportType") SupportType supportType,
            @Param("intelRxId") String intelRxId);


    @Query("SELECT t FROM SupportTicket t " +
            "LEFT JOIN t.supportType st " +
            "LEFT JOIN Pharmacy p ON p.intelRxId = t.intelRxId " +
            "LEFT JOIN p.contactInfo ci " +
            "WHERE (:supportType IS NULL OR st = :supportType) " +
            "AND (:intelRxId IS NULL OR p.intelRxId = :intelRxId) " +
            "AND (:state IS NULL OR ci.state = :state) " +
            "AND (COALESCE(:keyword, '') = '' OR " +
            "LOWER(t.subject) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ci.state) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.pharmacyName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<SupportTicket> findByFilters(
            @Param("supportType") SupportType supportType,
            @Param("intelRxId") String intelRxId,
            @Param("state") String state,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    Optional<SupportTicket> findByIdAndIntelRxId(Long id, String intelRxId);


}
