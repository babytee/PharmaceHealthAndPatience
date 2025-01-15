package com.pharmacy.intelrx.pharmacy.repositories.employee;

import com.pharmacy.intelrx.pharmacy.models.employee.Benefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BenefitRepository extends JpaRepository<Benefit,Long> {
    Optional<Benefit>findByEmployeeId(Long employeeId);
}
