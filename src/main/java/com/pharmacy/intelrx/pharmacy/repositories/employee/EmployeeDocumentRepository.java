package com.pharmacy.intelrx.pharmacy.repositories.employee;

import com.pharmacy.intelrx.pharmacy.models.employee.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument,Long> {
    Optional<EmployeeDocument>findByEmployeeId(Long employeeId);

    List<EmployeeDocument> findAllByEmployeeId(Long employeeId);
}
