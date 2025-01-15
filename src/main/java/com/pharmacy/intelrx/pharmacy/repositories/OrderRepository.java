package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.models.Patient;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findBySalesPersonIdAndStatus(Long salesPersonId, boolean status);

    Optional<Order> findByIdAndIntelRxIdAndPharmacyBranch(Long orderId, String intelRxId, PharmacyBranch pharmacyBranch);

    Optional<Order> findByIdAndIntelRxId(Long orderId, String intelRxId);

    Optional<Order> findByIdAndIntelRxIdAndStatus(Long orderId, String intelRxId, boolean status);

    Optional<Order> findByIntelRxIdAndPharmacyBranchAndPatientAndStatus(String intelRxId, PharmacyBranch pharmacyBranch, Patient patient, boolean status);

    Optional<Order> findByIdAndIntelRxIdAndPharmacyBranchAndPatientAndStatus(Long orderId, String intelRxId, PharmacyBranch pharmacyBranch, Patient patient, boolean status);

    List<Order> findByIntelRxIdAndPharmacyBranchAndStatus(String intelRxId, PharmacyBranch pharmacyBranch, boolean status);

    List<Order> findByIntelRxIdAndStatus(String intelRxId, boolean status);

    @Query("SELECT o FROM Order o WHERE o.id = :id " +
            "AND o.intelRxId = :intelRxId " +
            "AND (:branchId IS NULL OR o.pharmacyBranch.id = :branchId) " +
            "AND (o.cashier = :user)")
    Optional<Order> findByIdAndCashier(
            @Param("id") Long id,
            @Param("intelRxId") String intelRxId,
            @Param("branchId") Long branchId,
            @Param("user") User user
    );

    @Query("SELECT o FROM Order o WHERE o.id = :id " +
            "AND o.intelRxId = :intelRxId " +
            "AND (:branchId IS NULL OR o.pharmacyBranch.id = :branchId) " +
            "AND (o.salesPerson = :user)")
    Optional<Order> findByIdAndSalesPerson(
            @Param("id") Long id,
            @Param("intelRxId") String intelRxId,
            @Param("branchId") Long branchId,
            @Param("user") User user
    );


    @Query(nativeQuery = true, value =
            "SELECT o.* FROM orders o " +
                    "LEFT JOIN patients p ON o.patient_id = p.id " +
                    "WHERE " +
                    "(:intelRxId IS NULL OR o.intel_rx_id = cast(:intelRxId as varchar)) AND " +
                    "(:branchId IS NULL OR o.pharmacy_branch_id = cast(:branchId as bigint)) AND " +
                    "((CAST(p.name AS text) ILIKE CAST(CONCAT('%', :searchTerm, '%') AS text)) OR " +
                    "(CAST(p.gender AS text) ILIKE CAST(CONCAT('%', :searchTerm, '%') AS text)) OR " +
                    "(CAST(p.phone_number AS text) ILIKE CAST(CONCAT('%', :searchTerm, '%') AS text)))")
    Page<Order> findByPatientsFilter(
            @Param("intelRxId") String intelRxId,
            @Param("branchId") Long branchId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);


    List<Order> findAllByOrderRef(String orderRef);

    List<Order> findAllByIntelRxId(String intelRxId);

    List<Order> findAllByIntelRxIdAndSalesStatus(String intelRxId, String salesStatus);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.patient.id = :patientId AND o.intelRxId = :intelRxId")
    Double getTotalAmountByPatientAndIntelRxId(@Param("patientId") Long patientId, @Param("intelRxId") String intelRxId);


    List<Order> findAllByIntelRxIdAndPatient(String intelRxId, Patient p);

    @Query("SELECT o FROM Order o WHERE o.patient.id = :patientId AND o.intelRxId <> :intelRxId")
    List<Order> findByPatientIdAndIntelRxIdNot(
            @Param("patientId") Long patientId,
            @Param("intelRxId") String intelRxId
    );

    List<Order> findAllByPatient(Patient p);

    @Query(nativeQuery = true, value =
            "SELECT * FROM orders o WHERE " +
                    "(:branchId IS NULL OR o.pharmacy_branch_id = cast(:branchId as bigint)) AND " +
                    "(:startDate IS NULL OR o.ordered_date >= cast(:startDate as timestamp without time zone)) AND " +
                    "(:endDate IS NULL OR o.ordered_date <= cast(:endDate as timestamp without time zone)) AND " +
                    "(:salesPersonId IS NULL OR o.sales_person_id = cast(:salesPersonId as bigint)) AND " +
                    "(:cashierId IS NULL OR o.cashier_id = cast(:cashierId as bigint)) AND " +
                    "(:intelRxId IS NULL OR o.intel_rx_id = cast(:intelRxId as varchar)) AND " +
                    "(:salesStatus IS NULL OR o.sales_status = cast(:salesStatus as varchar)) AND " +
                    "(:status IS NULL OR o.status = cast(:status as boolean))")
    Page<Order> findByFilters(
            @Param("branchId") Long branchId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("salesPersonId") Long salesPersonId,
            @Param("cashierId") Long cashierId,
            @Param("salesStatus") String salesStatus,
            @Param("intelRxId") String intelRxId,
            @Param("status") boolean status,
            Pageable pageable);


    @Query(nativeQuery = true, value =
            "SELECT * FROM orders o WHERE " +
                    "(:branchId IS NULL OR o.pharmacy_branch_id = cast(:branchId as bigint)) AND " +
                    "(:startDate IS NULL OR o.ordered_date >= cast(:startDate as timestamp without time zone)) AND " +
                    "(:endDate IS NULL OR o.ordered_date <= cast(:endDate as timestamp without time zone)) AND " +
                    "(:salesPersonId IS NULL OR o.sales_person_id = cast(:salesPersonId as bigint)) AND " +
                    "(:cashierId IS NULL OR o.cashier_id = cast(:cashierId as bigint)) AND " +
                    "(:intelRxId IS NULL OR o.intel_rx_id = cast(:intelRxId as varchar)) AND " +
                    "(:salesStatus IS NULL OR o.sales_status = cast(:salesStatus as varchar)) AND " +
                    "(:status IS NULL OR o.status = cast(:status as boolean))")
    List<Order> findByFiltersForSalesStart(
            @Param("branchId") Long branchId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("salesPersonId") Long salesPersonId,
            @Param("cashierId") Long cashierId,
            @Param("salesStatus") String salesStatus,
            @Param("intelRxId") String intelRxId,
            @Param("status") boolean status);


    // Custom query: Find orders by patient details, status, branch, and IntelRx with casting for PostgreSQL
    @Query("SELECT o FROM Order o WHERE " +
            "(LOWER(o.patient.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.patient.gender) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.patient.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:intelRxId IS NULL OR LOWER(o.intelRxId) = LOWER(:intelRxId)) AND " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:branch IS NULL OR o.pharmacyBranch = :branch)")
    List<Order> searchOrdersByPatientDetailsAndIntelRxIdAndStatusAndBranch(
            @Param("keyword") String keyword,
            @Param("intelRxId") String intelRxId,
            @Param("status") Boolean status,
            @Param("branch") PharmacyBranch branch
    );

    List<Order> findAllByPharmacyBranchIsNull();

    List<Order> findAllByIntelRxIdAndPharmacyBranch(String intelRxId, PharmacyBranch pharmacyBranch);

    List<Order> findAllByIntelRxIdAndPharmacyBranchAndStatus
            (String intelRxId, PharmacyBranch pharmacyBranch, boolean status);


    List<Order> findAllByIntelRxIdAndOrderedDateBetween(String intelRxId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.intelRxId = :intelRxId " +
            "AND o.orderedDate BETWEEN :startDate AND :endDate " +
            "AND (:pharmacyBranch is null OR o.pharmacyBranch = :pharmacyBranch OR o.pharmacyBranch is null)")
    List<Order> findAllByIntelRxIdAndOrderedDateBetweenAndPharmacyBranch(
            @Param("intelRxId") String intelRxId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("pharmacyBranch") PharmacyBranch pharmacyBranch
    );


    @Query("SELECT o FROM Order o WHERE o.intelRxId = :intelRxId " +
            "AND (:year is null OR EXTRACT(YEAR FROM o.orderedDate) = :year) " +
            "AND (:month is null OR EXTRACT(MONTH FROM o.orderedDate) = :month) " +
            "AND (:pharmacyBranch is null OR o.pharmacyBranch = :pharmacyBranch OR o.pharmacyBranch is null)")
    List<Order> findAllByIntelRxIdAndOrderedDateYearAndOrderedDateMonthAndPharmacyBranch(
            @Param("intelRxId") String intelRxId,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("pharmacyBranch") PharmacyBranch pharmacyBranch
    );


    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
            "WHERE YEAR(o.orderedDate) = :year " +
            "AND o.intelRxId = :intelRxId ")
    Double getTotalSalesForYear(@Param("year") int year,
                                @Param("intelRxId") String intelRxId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
            "WHERE o.orderedDate BETWEEN :startDate AND :endDate " +
            "AND o.intelRxId = :intelRxId ")
    Double getTotalRevenueForPeriod(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    @Param("intelRxId") String intelRxId);

    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE o.orderedDate BETWEEN :startDate AND :endDate " +
            "AND o.intelRxId = :intelRxId ")
    Long getNumberOfTransactionsForPeriod(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("intelRxId") String intelRxId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
            "WHERE o.orderedDate BETWEEN :startDate AND :endDate " +
            "AND o.intelRxId = :intelRxId ")
    Double getTotalSales(@Param("startDate") LocalDateTime startDate,
                         @Param("endDate") LocalDateTime endDate,
                         @Param("intelRxId") String intelRxId);

    @Query("SELECT SUM(ci.quantity) FROM Order o JOIN o.cartItem ci WHERE o.orderedDate BETWEEN :startDate AND :endDate AND o.intelRxId = :intelRxId")
    Integer getUnitsSold(@Param("startDate") LocalDateTime startDate,
                         @Param("endDate") LocalDateTime endDate,
                         @Param("intelRxId") String intelRxId);

    @Query("SELECT SUM(o.cashPayment + o.transferPayment + o.posPayment) FROM Order o " +
            "WHERE o.orderedDate BETWEEN :startDate AND :endDate " +
            "AND o.intelRxId = :intelRxId ")
    Double getTotalCashAndEquivalents(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      @Param("intelRxId") String intelRxId);

    @Query("SELECT SUM(o.balance) FROM Order o " +
            "WHERE o.orderedDate BETWEEN :startDate AND :endDate " +
            "AND o.intelRxId = :intelRxId ")
    Double getTotalAccountsReceivable(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      @Param("intelRxId") String intelRxId);

    @Query("SELECT SUM(ci.amount * ci.quantity) FROM CartItem ci JOIN ci.order o " +
            "WHERE o.orderedDate BETWEEN :startDate AND :endDate " +
            "AND o.intelRxId = :intelRxId ")
    Double getTotalCOGS(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("intelRxId") String intelRxId);

}
