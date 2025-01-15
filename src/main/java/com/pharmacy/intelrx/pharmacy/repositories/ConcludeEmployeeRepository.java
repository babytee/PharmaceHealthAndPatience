package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.ConcludeEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConcludeEmployeeRepository extends JpaRepository<ConcludeEmployee,Long> {
    Optional<ConcludeEmployee>findByEmployeeIdAndEmployeeIntelRxId(Long id,String employeeIntelRxId);
}
