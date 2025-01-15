package com.pharmacy.intelrx.pharmacy.repositories.employee;

import com.pharmacy.intelrx.pharmacy.models.employee.Legal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LegalRepository extends JpaRepository<Legal,Long> {
    Optional<Legal> findByEmployeeId(Long employeeId);
}
