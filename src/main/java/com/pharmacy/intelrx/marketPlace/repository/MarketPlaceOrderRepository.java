package com.pharmacy.intelrx.marketPlace.repository;

import com.pharmacy.intelrx.marketPlace.models.MarketPlaceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarketPlaceOrderRepository extends JpaRepository<MarketPlaceOrder, Long> {

    // Search by orderRef and filter by salesStatus
    @Query("SELECT o FROM MarketPlaceOrder o WHERE " +
            "(:intelRxId IS NULL OR o.intelRxId = :intelRxId) AND " +
            "(:orderRef IS NULL OR o.orderRef LIKE %:orderRef%) AND " +
            "(:salesStatus IS NULL OR o.salesStatus = :salesStatus)")
    Page<MarketPlaceOrder> findByIntelRxIdAndOrderRefAndSalesStatus(
            @Param("intelRxId") String intelRxId,
            @Param("orderRef") String orderRef,
            @Param("salesStatus") String salesStatus,
            Pageable pageable);

    @Query(value = "SELECT AVG(o.total_amount) " +
            "FROM market_place_orders o " +
            "WHERE o.intel_rx_id IS NOT NULL " +
            "AND EXTRACT(MONTH FROM o.ordered_date) = :month " +
            "AND EXTRACT(YEAR FROM o.ordered_date) = :year",
            nativeQuery = true)
    Optional<Double> findAverageOrderAmountByMonthAndYear(
            @Param("month") int month,
            @Param("year") int year
    );





    // 1. Total amount for all orders by intelRxId
    @Query("SELECT SUM(o.totalAmount) FROM MarketPlaceOrder o " +
            "WHERE (:intelRxId IS NULL OR o.intelRxId = :intelRxId)")
    Optional<Double> findTotalOrderAmountByIntelRxId(@Param("intelRxId") String intelRxId);

    // 2. Total amount for "Medication" items in a specific order
    @Query("SELECT SUM(c.amount) FROM MarketPlaceOrder o JOIN o.cartItem c " +
            "WHERE (:intelRxId IS NULL OR o.intelRxId = :intelRxId) " +
            "AND c.inventory.inventoryType = 'MEDICATION'")
    Optional<Double> findTotalMedicationAmountByOrderId(@Param("intelRxId") String intelRxId);

    // 3. Total amount for "Grocery" items in a specific order
    @Query("SELECT SUM(c.amount) FROM MarketPlaceOrder o JOIN o.cartItem c " +
            "WHERE (:intelRxId IS NULL OR o.intelRxId = :intelRxId) " +
            "AND c.inventory.inventoryType = 'GROCERY'")
    Optional<Double> findTotalGroceryAmountByOrderId(@Param("intelRxId") String intelRxId);

    // Count of "Medication" items in a specific order
    @Query("SELECT COUNT(c) FROM MarketPlaceOrder o JOIN o.cartItem c " +
            "WHERE (:intelRxId IS NULL OR o.intelRxId = :intelRxId) " +
            "AND c.inventory.inventoryType = 'MEDICATION'")
    Optional<Integer> countMedicationItemsByOrderId(@Param("intelRxId") String intelRxId);

    // Count of "Grocery" items in a specific order
    @Query("SELECT COUNT(c) FROM MarketPlaceOrder o JOIN o.cartItem c " +
            "WHERE (:intelRxId IS NULL OR o.intelRxId = :intelRxId) " +
            "AND c.inventory.inventoryType = 'GROCERY'")
    Optional<Integer> countGroceryItemsByOrderId(@Param("intelRxId") String intelRxId);


}
