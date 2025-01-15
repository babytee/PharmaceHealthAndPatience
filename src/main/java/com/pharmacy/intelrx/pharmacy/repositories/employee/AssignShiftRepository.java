package com.pharmacy.intelrx.pharmacy.repositories.employee;

import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.employee.AssignShift;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public interface AssignShiftRepository extends JpaRepository<AssignShift,Long> {
    Optional<AssignShift>findByIdAndIntelRxIdAndPharmacyBranch(Long id, String intelRxId, PharmacyBranch pharmacyBranch);

    Optional<AssignShift>findByAssignedMemberAndIntelRxIdAndPharmacyBranch(Employee assignedMember, String intelRxId, PharmacyBranch pharmacyBranch);

    Optional<AssignShift> findByAssignedMemberAndIntelRxIdAndPharmacyBranchAndStartDate(
            Employee assignedMember,
            String intelRxId,
            PharmacyBranch pharmacyBranch,
            LocalDate startDate);

    List<AssignShift>findAllByAssignedMemberAndIntelRxIdAndPharmacyBranch(Employee assignedMember, String intelRxId, PharmacyBranch pharmacyBranch);

    Optional<AssignShift>findByIdAndAssignedMemberAndIntelRxIdAndPharmacyBranch(Long id,Employee assignedMember, String intelRxId, PharmacyBranch pharmacyBranch);

    @Query(nativeQuery = true, value =
            "SELECT * FROM assign_shift a WHERE" +
            "(:startDate IS NULL OR a.start_date >= cast(:startDate as timestamp without time zone)) " +
            "AND (:endDate IS NULL OR a.start_date <= cast(:endDate as timestamp without time zone)) " +
            "AND (:jobTitleId IS NULL OR a.job_title_id = cast(:jobTitleId as bigint)) " +
            "AND (:branchId IS NULL OR a.pharmacy_branch_id = cast(:branchId as bigint)) " +
            "AND (:shiftTime IS NULL OR a.shift_time = cast(:shiftTime as varchar)) " +
            "AND (:intelRxId IS NOT NULL AND a.intel_rx_id = cast(:intelRxId as varchar)) " +
            "AND (:assignedMemberId IS NULL OR a.assigned_member_id = cast(:assignedMemberId as bigint))")
    List<AssignShift> filterAssignShifts(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("jobTitleId") Long jobTitleId,
            @Param("branchId") Long branchId,
            @Param("shiftTime") String shiftTime,
            @Param("intelRxId") String intelRxId,
            @Param("assignedMemberId") Long assignedMemberId
    );

    @Query("SELECT COUNT(a) > 0 FROM AssignShift a WHERE DATE(a.createdAt) = :currentDate AND a.intelRxId = :intelRxId")
    boolean existsByCreatedAtAndIntelRxId(@Param("currentDate") LocalDate currentDate, @Param("intelRxId") String intelRxId);

//    @Query("SELECT COUNT(a) > 0 FROM AssignShift a WHERE DATE(a.startDate) = :currentDate AND a.intelRxId = :intelRxId AND a.assignedMember.id = :assignedMemberId")
//    boolean existsByCreatedAtAndIntelRxIdAndAssignedMemberId(
//            @Param("currentDate") LocalDate currentDate,
//            @Param("intelRxId") String intelRxId,
//            @Param("assignedMemberId") Long assignedMemberId);

    @Query("SELECT COUNT(a) > 0 FROM AssignShift a WHERE DATE(a.startDate) = :currentDate AND a.intelRxId = :intelRxId AND a.assignedMember.id = :assignedMemberId")
    boolean existsByStartDateAndIntelRxIdAndAssignedMemberId(
            @Param("currentDate") LocalDate currentDate,
            @Param("intelRxId") String intelRxId,
            @Param("assignedMemberId") Long assignedMemberId);


    Optional<AssignShift> findFirstByPharmacyBranchAndIntelRxIdAndAssignedMemberIdOrderByStartDateDesc(PharmacyBranch pharmacyBranch, String intelRxId, Long memberId);

    List<AssignShift>findAllByStartDate(LocalDate currentDate);


}
