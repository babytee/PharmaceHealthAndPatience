package com.pharmacy.intelrx.pharmacy.repositories.employee;

import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeIntelRxId(String intelRxId);


    Optional<Employee> findFirstByEmployeeIntelRxId(String intelRxId);

    @Query("SELECT e FROM Employee e WHERE e.id = :id AND e.employeeIntelRxId = :intelRxId")
    Optional<Employee> findByIdAndEmployeeIntelRxId(
            @Param("id") Long id,
            @Param("intelRxId") String intelRxId
    );

    @Query(value = "SELECT e.* FROM employees e " +
            "LEFT JOIN job_information ji ON e.id = ji.employee_id " +
            "WHERE (:jobTitleId IS NULL OR ji.job_title_id = :jobTitleId) AND " +
            "(:intelRxId IS NULL OR e.employee_intel_rx_id = :intelRxId)", nativeQuery = true)
    List<Employee> findByIntelRxIdAndJobTitleId(
            @Param("intelRxId") String intelRxId,
            @Param("jobTitleId") Long jobTitleId);




    Optional<Employee> findByUserId(Long userId);

    Optional<Employee> findByUserIdAndEmployeeIntelRxId(Long userId,String intelRxId);

    List<Employee> findAllByEmployeeIntelRxId(String intelRxId);

    List<Employee> findAllByEmployeeIntelRxIdAndStatus(String intelRxId,boolean status);

    @Query(value = "SELECT e.id, e.user_id, e.employee_type, e.employee_intel_rx_id, " +
            "u.user_status, u.birth_month, be.pharmacy_branch_id as branch_id, " +
            "cd.id as compensation_detail_id, cd.salary_type_id, cd.salary_status, " +
            "ji.id as job_information_id, ji.seniority_level_id, " +
            "ji.job_title_id, ji.department_id, ji.work_schedule_id, ji.start_date, ji.end_date, " +
            "ji.job_scope, e.created_at, e.updated_at, e.status, " +
            "CONCAT(u.first_name, ' ', u.last_name) AS full_name, ci.state, ci.country, " +
            "p.pharmacy_name " +
            "FROM employees e " +
            "LEFT JOIN compensation_details cd ON e.id = cd.employee_id " +
            "LEFT JOIN job_information ji ON e.id = ji.employee_id " +
            "LEFT JOIN branch_employees be ON e.id = be.employee_id " +
            "LEFT JOIN users u ON e.user_id = u.id " +
            "LEFT JOIN contact_info ci ON e.id = ci.employee_id " +
            "LEFT JOIN pharmacies p ON e.employee_intel_rx_id = p.intel_rx_id " +
            "WHERE (:userId IS NULL OR u.id = cast(:userId as bigint)) AND " +
            "(:workerStatus IS NULL OR LOWER(u.user_status) = LOWER(cast(:workerStatus as text))) AND " +
            "(:employeeType IS NULL OR LOWER(e.employee_type) = LOWER(cast(:employeeType as text))) AND " +
            "(:salaryStatus IS NULL OR LOWER(cd.salary_status) = LOWER(cast(:salaryStatus as text))) AND " +
            "(:salaryTypeId IS NULL OR cd.salary_type_id = cast(:salaryTypeId as bigint)) AND " +
            "(:jobTitleId IS NULL OR ji.job_title_id = cast(:jobTitleId as bigint)) AND " +
            "(:birthMonth IS NULL OR u.birth_month = cast(:birthMonth as integer)) AND " +
            "(:intelRxId IS NULL OR e.employee_intel_rx_id = cast(:intelRxId as text)) AND " +
            "(:branchId IS NULL OR be.pharmacy_branch_id = cast(:branchId as bigint)) AND " +
            "(:searchText IS NULL OR " +
            "   LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "   LOWER(ci.state) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "   LOWER(ci.country) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "   LOWER(p.pharmacy_name) LIKE LOWER(CONCAT('%', :searchText, '%')))",
            nativeQuery = true)
    <T> Page<Employee> findAllByFilterRequest(
            @Param("userId") Long userId,
            @Param("workerStatus") String workerStatus,
            @Param("employeeType") String employeeType,
            @Param("salaryStatus") String salaryStatus,
            @Param("salaryTypeId") Long salaryTypeId,
            @Param("jobTitleId") Long jobTitleId,
            @Param("branchId") Long branchId,
            @Param("birthMonth") Integer birthMonth,
            @Param("intelRxId") String intelRxId,
            @Param("searchText") String searchText,
            Pageable pageable);



    List<Employee> findAllByEmployeeIntelRxIdAndEmployeeType(
            String intelRxId, String employeeType
    );

}
