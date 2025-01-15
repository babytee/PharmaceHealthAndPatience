package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.Expenditure;
import com.pharmacy.intelrx.pharmacy.models.Order;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
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
public interface ExpenditureRepository extends JpaRepository<Expenditure, Long> {
    Optional<Expenditure> findByIdAndIntelRxId(Long id, String intelRxId);

    @Query("SELECT e FROM Expenditure e WHERE e.intelRxId = :intelRxId " +
            "AND e.createdAt BETWEEN :startDate AND :endDate " +
            "AND (:expenditureType IS NULL OR e.expenditureType = :expenditureType) " +
            "AND (:pharmacyBranch IS NULL OR e.branch = :pharmacyBranch)")
    List<Expenditure> findAllByIntelRxIdAndCreatedAtBetweenAndExpenditureTypeAndBranch(
            @Param("intelRxId") String intelRxId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("expenditureType") String expenditureType,
            @Param("pharmacyBranch") PharmacyBranch pharmacyBranch
    );

    @Query("SELECT e FROM Expenditure e WHERE e.intelRxId = :intelRxId " +
            "AND (:year IS NULL OR EXTRACT(YEAR FROM e.createdAt) = :year) " +
            "AND (:month IS NULL OR EXTRACT(MONTH FROM e.createdAt) = :month) " +
            "AND (:expenditureType IS NULL OR e.expenditureType = :expenditureType) " +
            "AND (:pharmacyBranch IS NULL OR e.branch = :pharmacyBranch)")
    List<Expenditure> findAllByIntelRxIdAndCreatedAtYearAndCreatedAtMonthAndBranch(
            @Param("intelRxId") String intelRxId,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("expenditureType") String expenditureType,
            @Param("pharmacyBranch") PharmacyBranch pharmacyBranch
    );


    @Query("SELECT COALESCE(SUM(e.amountSpent), 0) FROM Expenditure e WHERE e.intelRxId = :intelRxId")
    Double getTotalAmountByIntelRxId(@Param("intelRxId") String intelRxId);

    @Query("SELECT COALESCE(SUM(e.amountSpent), 0) FROM Expenditure e WHERE " +
            "e.intelRxId = :intelRxId AND " +
            "e.approved = :approved AND" +
            "(:branchId IS NULL OR e.branch.id = :branchId)")
    Double getTotalAmountByIntelRxIdAndApprovedAndBranch
            (
                    @Param("intelRxId") String intelRxId,
                    @Param("approved") boolean approved,
                    @Param("branchId") Long branchId
            );

    List<Expenditure> findAllByIntelRxIdAndBranch(String intelRxId, PharmacyBranch branch);

    List<Expenditure> findAllByIntelRxIdAndBranchAndApproved
            (String intelRxId, PharmacyBranch branch, boolean approved);

    @Query("SELECT e FROM Expenditure e WHERE " +
            "(:expenseName IS NULL OR LOWER(e.expenseName) LIKE LOWER(concat('%', :expenseName, '%'))) AND " +
            "(:branchId IS NULL OR e.branch.id = :branchId) AND " +
            "(:intelRxId IS NOT NULL AND e.intelRxId = :intelRxId)")
    Page<Expenditure> findByFilters(
            @Param("expenseName") String expenseName,
            @Param("branchId") Long branchId,
            @Param("intelRxId") String intelRxId,
            Pageable pageable
    );


    List<Expenditure> findAllByIntelRxId(String intelRxId);

    List<Expenditure> findAllByIntelRxIdAndCreatedAtBetween(String intelRxId, LocalDateTime startDate, LocalDateTime endDate);


    List<Expenditure> findAllByBranchIdAndIntelRxIdAndExpenditureTypeOrderByAmountSpentDesc(
            @Param("branchId") Long branchId,
            @Param("intelRxId") String intelRxId,
            @Param("expenditureType") String expenditureType);

    @Query("SELECT SUM(e.amountSpent) FROM Expenditure e " +
            "WHERE e.createdAt BETWEEN :startDate AND :endDate " +
            "AND e.intelRxId = :intelRxId ")
    Double getTotalExpenditures(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                @Param("intelRxId") String intelRxId);

    @Query("SELECT AVG(e.amountSpent) FROM Expenditure e " +
            "WHERE e.createdAt BETWEEN :startDate AND :endDate " +
            "AND e.intelRxId = :intelRxId " +
            "AND e.expenditureType = :expenditureType")
    Double getAverageAccountsPayable(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("intelRxId") String intelRxId,
                                     @Param("expenditureType") String expenditureType);

    @Query("SELECT COALESCE(SUM(e.amountSpent), 0.0) " +
            "FROM Expenditure e " +
            "WHERE e.expenditureType = :expenditureType " +
            "AND e.createdAt >= :startDate " +
            "AND e.createdAt <= :endDate " +
            "AND e.intelRxId = :intelRxId")
    Double getTotalAccountsPayable(LocalDateTime startDate, LocalDateTime endDate, String intelRxId, String expenditureType);


}
