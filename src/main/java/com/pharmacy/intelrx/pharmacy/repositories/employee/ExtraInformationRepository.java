package com.pharmacy.intelrx.pharmacy.repositories.employee;

import com.pharmacy.intelrx.pharmacy.models.employee.ExtraInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExtraInformationRepository extends JpaRepository<ExtraInformation, Long> {
    Optional<ExtraInformation> findByEmployeeId(Long employeeId);
}
