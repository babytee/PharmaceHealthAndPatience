package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.BranchEmployee;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchEmployeeRepository extends JpaRepository<BranchEmployee, Long> {
    Optional<BranchEmployee> findByPharmacyBranchId(Long pharmacyBranchId);

    Optional<BranchEmployee> findByPharmacyBranchAndEmployee(PharmacyBranch pharmacyBranch, Employee employee);

    Optional<BranchEmployee> findByPharmacyBranchIdAndEmployee(Long pharmacyBranchId, Employee employee);

    List<BranchEmployee> findAllByPharmacyBranchId(Long pharmacyBranchId);

    Optional<BranchEmployee>findByEmployee(Employee employee);
}
