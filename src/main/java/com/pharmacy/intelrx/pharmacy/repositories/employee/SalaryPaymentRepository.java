package com.pharmacy.intelrx.pharmacy.repositories.employee;

import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.SalaryPayment;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public interface SalaryPaymentRepository extends JpaRepository<SalaryPayment, Long> {
    Page<SalaryPayment> findAllByIntelRxIdAndPharmacyBranchAndEmployee(
            String intelRxId, PharmacyBranch branch,
            Employee employee, Pageable pageable
    );

    List<SalaryPayment> findByIntelRxIdAndPharmacyBranchAndEmployee(
            String intelRxId, PharmacyBranch branch, Employee employee
    );

    List<SalaryPayment> findAllByIntelRxIdAndEmployee(
            String intelRxId, Employee employee
    );

    List<SalaryPayment> findByIntelRxIdAndPharmacyBranchAndEmployeeAndStatus(
            String intelRxId, PharmacyBranch branch, Employee employee,String status
    );

    List<SalaryPayment> findAllByIntelRxIdAndEmployeeAndStatus(
            String intelRxId, Employee employee,String status
    );

    Optional<SalaryPayment> findByPharmacyBranchAndIntelRxIdAndEmployee(
            PharmacyBranch branch, String intelRxId, Employee employee
    );

    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END " +
            "FROM SalaryPayment sp " +
            "WHERE sp.employee.id = :employeeId " +
            "AND sp.payPeriod = :currentMonth " +
            "AND sp.status = 'Paid'")
    boolean isSalaryPaidForCurrentMonth(@Param("employeeId") Long employeeId, @Param("currentMonth") String currentMonth);

    default String getCurrentMonth() {
        return LocalDateTime.now().getMonth().name();
    }

    default String getCurrentYearMonth() {
        // Get the current YearMonth
        YearMonth currentYearMonth = YearMonth.now();

        // Define a formatter for the desired format "MMMM yyyy"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

        // Format the current YearMonth into the desired format
        String formattedYearMonth = currentYearMonth.format(formatter);
        return formattedYearMonth;
    }

    @Query("SELECT COUNT(sp) " +
            "FROM SalaryPayment sp " +
            "WHERE sp.employee.id = :employeeId " +
            "AND sp.payPeriod = (SELECT MAX(spp.payPeriod) FROM SalaryPayment spp WHERE spp.employee.id = :employeeId) " +
            "AND sp.status = 'Paid' " +
            "AND :currentDate > sp.payPeriod")
    int countOverduePayments(@Param("employeeId") Long employeeId,
                             @Param("currentDate") String currentDate);

}
