package com.pharmacy.intelrx.pharmacy.repositories.employee;

import com.pharmacy.intelrx.pharmacy.models.employee.CompensationDetail;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompensationDetailRepository extends JpaRepository<CompensationDetail,Long> {
    Optional<CompensationDetail>findByEmployeeId(Long employeeId);

    List<CompensationDetail>findAllByEmployee(Employee employee);
}
