package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.TopSellingItemProjection;
import com.pharmacy.intelrx.pharmacy.models.*;
import jakarta.persistence.TemporalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByIntelRxIdAndInventoryAndOrderAndPatientAndUserAndStatus(String intelRxId, Inventory inventory, Order order, Patient patient, User user, boolean status);

    Optional<CartItem> findByIntelRxIdAndInventoryAndPatientAndUserAndStatus(String intelRxId, Inventory inventory, Patient patient, User user, boolean status);

    List<CartItem> findByIntelRxIdAndOrderAndPatientAndUserAndStatus(String intelRxId, Order order, Patient patient, User user, boolean status);

    Optional<CartItem> findByIntelRxIdAndInventoryAndPharmacyBranchAndOrderAndPatientAndStatus(String intelRxId, Inventory inventory, PharmacyBranch pharmacyBranch, Order order, Patient patient, boolean status);

    List<CartItem> findByIntelRxIdAndPharmacyBranchAndOrderAndPatientAndStatus(String intelRxId, PharmacyBranch pharmacyBranch, Order order, Patient patient, boolean status);

    List<CartItem> findByIntelRxIdAndPatientAndStatus(String intelRxId, Patient patient, boolean status);

    Optional<CartItem> findByIdAndIntelRxId(Long id, String intelRxId);


    Page<CartItem> findByIntelRxIdAndPharmacyBranchAndOrderAndUserAndStatus(
            String intelRxId, PharmacyBranch pharmacyBranch, Order order, User user,
            boolean status, Pageable pageable);

    Page<CartItem> findByIntelRxIdAndPharmacyBranchAndOrderAndStatus(
            String intelRxId, PharmacyBranch pharmacyBranch, Order order,
            boolean status, Pageable pageable);


    List<CartItem> findByIntelRxIdAndPharmacyBranchAndOrderAndUser(String intelRxId, PharmacyBranch pharmacyBranch, Order order, User user);

    List<CartItem> findByIntelRxIdAndAndOrder(String intelRxId, Order order);

    List<CartItem> findByIntelRxIdAndAndOrderAndStatus(String intelRxId, Order order, boolean status);

    Page<CartItem> findByOrderAndIntelRxIdAndStatus(Order order, String intelRxId, boolean status, Pageable pageable);

    Page<CartItem> findByOrderAndIntelRxIdAndStatusAndPharmacyBranchAndUser(
            Order order, String intelRxId,
            PharmacyBranch pharmacyBranch,
            boolean status, User user,
            Pageable pageable);

    List<CartItem> findByIntelRxIdAndUserAndStatus(String intelRxId, User user, boolean status);

    List<CartItem> findByIntelRxIdAndPharmacyBranchAndUserAndStatus(
            String intelRxId,
            PharmacyBranch pharmacyBranch,
            User user,
            boolean status);

    @Query("SELECT ci FROM CartItem ci WHERE ci.intelRxId = :intelRxId " +
            "AND ci.createdAt BETWEEN :startDate AND :endDate " +
            "AND (:pharmacyBranch is null OR ci.pharmacyBranch = :pharmacyBranch) " +
            "AND ci.status = :status")
    List<CartItem> findAllByIntelRxIdAndCreatedAtBetweenAndPharmacyBranchAndStatus(
            @Param("intelRxId") String intelRxId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("pharmacyBranch") PharmacyBranch pharmacyBranch,
            @Param("status") boolean status
    );

    @Query("SELECT ci FROM CartItem ci " +
            "LEFT JOIN ci.inventory inv WHERE ci.intelRxId = :intelRxId " +
            "AND ci.createdAt BETWEEN :startDate AND :endDate " +
            "AND (:pharmacyBranch IS NULL OR ci.pharmacyBranch = :pharmacyBranch) " +
            "AND ci.status = :status " +
            "AND (:inventoryType IS NULL OR inv.inventoryType = :inventoryType)")
    List<CartItem> findAllByIntelRxIdAndCreatedAtBetweenAndPharmacyBranchAndStatusAndInventoryType(
            @Param("intelRxId") String intelRxId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("pharmacyBranch") PharmacyBranch pharmacyBranch,
            @Param("status") boolean status,
            @Param("inventoryType") String inventoryType
    );


    @Query("SELECT ci FROM CartItem ci WHERE ci.intelRxId = :intelRxId " +
            "AND EXTRACT(YEAR FROM ci.createdAt) = :year " +
            "AND EXTRACT(MONTH FROM ci.createdAt) = :month " +
            "AND (:pharmacyBranch IS NULL OR ci.pharmacyBranch = :pharmacyBranch) " +
            "AND ci.status = :status")
    List<CartItem> findAllByIntelRxIdAndCreatedAtYearAndCreatedAtMonthAndPharmacyBranchAndStatus(
            @Param("intelRxId") String intelRxId,
            @Param("year") int year,
            @Param("month") int month,
            @Param("pharmacyBranch") PharmacyBranch pharmacyBranch,
            @Param("status") boolean status
    );


    @Query("SELECT ci FROM CartItem ci " +
            "LEFT JOIN ci.inventory inv WHERE ci.intelRxId = :intelRxId " +
            "AND EXTRACT(YEAR FROM ci.createdAt) = :year " +
            "AND EXTRACT(MONTH FROM ci.createdAt) = :month " +
            "AND (:pharmacyBranch IS NULL OR ci.pharmacyBranch = :pharmacyBranch) " +
            "AND ci.status = :status " +  // Added space before AND
            "AND (:inventoryType IS NULL OR inv.inventoryType = :inventoryType)")
    List<CartItem> findAllByIntelRxIdAndCreatedAtYearAndCreatedAtMonthAndPharmacyBranchAndStatusAndInventoryType(
            @Param("intelRxId") String intelRxId,
            @Param("year") int year,
            @Param("month") int month,
            @Param("pharmacyBranch") PharmacyBranch pharmacyBranch,
            @Param("status") boolean status,
            @Param("inventoryType") String inventoryType
    );


    @Query("SELECT i.itemName AS itemName, " +
            "SUM(ci.amount * ci.quantity) AS totalSalesAmount, " +
            "COALESCE(SUM(prev.amount * prev.quantity), 0) AS previousSalesAmount " +
            "FROM CartItem ci " +
            "JOIN ci.inventory i " +
            "LEFT JOIN CartItem prev ON ci.inventory.id = prev.inventory.id " +
            "AND ci.pharmacyBranch.id = prev.pharmacyBranch.id " +
            "AND ci.intelRxId = prev.intelRxId " +
            "AND prev.createdAt < CURRENT_DATE " +
            "WHERE (:pharmacyBranchId IS NULL OR ci.pharmacyBranch.id = :pharmacyBranchId) " +
            "AND ci.intelRxId = :intelRxId " +
            "GROUP BY i.itemName " +
            "ORDER BY totalSalesAmount DESC")
    List<TopSellingItemProjection> findTopSellingInventoryItemsByBranchAndIntelRxId
            (
                    @Param("pharmacyBranchId") Long pharmacyBranchId,
                    @Param("intelRxId") String intelRxId
            );


    @Query("SELECT i.itemName AS itemName, " +
            "SUM(ci.amount * ci.quantity) AS totalSalesAmount, " +
            "COALESCE(SUM(prev.amount * prev.quantity), 0) AS previousSalesAmount " +
            "FROM CartItem ci " +
            "JOIN ci.inventory i " +
            "LEFT JOIN CartItem prev ON ci.inventory.id = prev.inventory.id " +
            "AND ci.intelRxId = prev.intelRxId " +
            "AND prev.createdAt < :startDate " +
            "WHERE EXISTS (SELECT 1 FROM Pharmacy p WHERE i.intelRxId = p.intelRxId" +
            " AND p.contactInfo.country = :country) " +
            "AND (:startDate IS NULL OR ci.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR ci.createdAt <= :endDate) " +
            "AND (:brand IS NULL OR i.brand.name = :brand) " +
            "AND (:medication IS NULL OR i.itemName = :medication) " +
            "GROUP BY i.itemName " +
            "ORDER BY totalSalesAmount DESC")
    List<TopSellingItemProjection> findTopSellingMedications(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("country") String country,
            @Param("brand") String brand,
            @Param("medication") String medication);


}
