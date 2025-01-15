package com.pharmacy.intelrx.marketPlace.repository;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.marketPlace.models.MarketPlaceCartItem;
import com.pharmacy.intelrx.marketPlace.models.MarketPlaceOrder;
import com.pharmacy.intelrx.pharmacy.TopSellingItemProjection;
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
public interface MarketPlaceCartItemRepository extends JpaRepository<MarketPlaceCartItem, Long> {


    List<MarketPlaceCartItem> findByIntelRxIdAndOrderAndUser
            (String intelRxId, MarketPlaceOrder order, User user);

    List<MarketPlaceCartItem> findByIntelRxIdAndUserAndStatus
            (String intelRxId, User user, boolean status);

    Page<MarketPlaceCartItem> findByIntelRxIdAndOrder
            (String intelRxId, MarketPlaceOrder order, Pageable pageable);

    Optional<MarketPlaceCartItem> findByIdAndIntelRxId(Long id, String intelRxId);

    @Query("SELECT m FROM MarketPlaceCartItem m " +
            "WHERE (:supplierPharmacy IS NULL OR m.supplierPharmacy = :supplierPharmacy) " +
            "AND (:salesStatus IS NULL OR m.salesStatus = :salesStatus)")
    Page<MarketPlaceCartItem> findBySalesStatusAndSupplierPharmacy(
            @Param("salesStatus") String salesStatus,
            @Param("supplierPharmacy") Pharmacy supplierPharmacy,
            Pageable pageable);

    @Query("SELECT AVG(m.amount) " +
            "FROM MarketPlaceCartItem m " +
            "WHERE m.supplierPharmacy IS NOT NULL " +
            "AND EXTRACT(YEAR FROM m.createdAt) = :year " +
            "AND EXTRACT(MONTH FROM m.createdAt) = :month")
    Optional<Double> findTotalAmountByDate(
            @Param("year") int year,
            @Param("month") int month
    );



    @Query("SELECT SUM(m.amount) FROM MarketPlaceCartItem m WHERE m.supplierPharmacy = :supplierPharmacy")
    Optional<Double> findTotalAmountByIntelRxId(@Param("supplierPharmacy") Pharmacy supplierPharmacy);

    @Query("SELECT SUM(m.amount) FROM MarketPlaceCartItem m WHERE m.supplierPharmacy = :supplierPharmacy AND " +
            "m.inventory.inventoryType = 'MEDICATION'")
    Optional<Double> findTotalMedicationAmountByIntelRxId(@Param("supplierPharmacy") Pharmacy supplierPharmacy);

    @Query("SELECT SUM(m.amount) FROM MarketPlaceCartItem m WHERE m.supplierPharmacy = :supplierPharmacy AND " +
            "m.inventory.inventoryType = 'GROCERY'")
    Optional<Double> findTotalGroceryAmountByIntelRxId(@Param("supplierPharmacy") Pharmacy supplierPharmacy);

    @Query("SELECT c FROM MarketPlaceCartItem c " +
            "WHERE (:intelRxId IS NULL OR c.intelRxId = :intelRxId) " +
            "AND c.status = true " +
            "AND (CASE WHEN :salesStatus IS NULL AND :search IS NULL THEN c.salesStatus = 'Delivered' " +
            "ELSE (:salesStatus IS NULL OR c.salesStatus = :salesStatus) END) " +
            "AND (COALESCE(:search, '') = '' OR LOWER(c.inventory.itemName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.supplierPharmacy.pharmacyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<MarketPlaceCartItem> findByIntelRxIdAndSalesStatusAndSearchFilter(
            @Param("intelRxId") String intelRxId,
            @Param("salesStatus") String salesStatus,
            @Param("search") String search,
            Pageable pageable);


    @Query("SELECT c FROM MarketPlaceCartItem c " +
            "WHERE (:intelRxId IS NULL OR c.intelRxId = :intelRxId) " +
            "AND c.status = true " +
            "AND (CASE WHEN :salesStatus IS NULL THEN c.salesStatus != 'Delivered' " +
            "ELSE c.salesStatus = :salesStatus END) " +
            "AND (COALESCE(:search, '') = '' OR LOWER(c.inventory.itemName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.supplierPharmacy.pharmacyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<MarketPlaceCartItem> filterPendingSearch(
            @Param("intelRxId") String intelRxId,
            @Param("salesStatus") String salesStatus,
            @Param("search") String search,
            Pageable pageable);


    @Query("SELECT i.itemName AS itemName, " +
            "SUM(mc.amount * mc.quantity) AS totalSalesAmount, " +
            "COALESCE(SUM(prev.amount * prev.quantity), 0) AS previousSalesAmount " +
            "FROM MarketPlaceCartItem mc " +
            "JOIN mc.inventory i " +
            "LEFT JOIN MarketPlaceCartItem prev ON mc.inventory.id = prev.inventory.id " +
            "AND mc.intelRxId = prev.intelRxId " +
            "AND EXTRACT(MONTH FROM prev.createdAt) = :month " +
            "AND prev.createdAt < CURRENT_DATE " +
            "WHERE mc.intelRxId IS NOT NULL " +
            "AND EXTRACT(MONTH FROM mc.createdAt) = :month " +
            "GROUP BY i.itemName " +
            "ORDER BY totalSalesAmount DESC")
    List<TopSellingItemProjection> findTopSellingInventoryItemsByMonth(
            @Param("month") int month
    );





}
