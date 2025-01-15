package com.pharmacy.intelrx.pharmacy.repositories.employee;


import com.pharmacy.intelrx.pharmacy.models.employee.WorkHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkHistoryRepository extends JpaRepository<WorkHistory,Long> {
    Optional<WorkHistory>findByJobTitle(String jobTitle);

    List<WorkHistory> findAllByEducationId(Long educationId);

    Optional<WorkHistory> findByEducationId(Long educationId);
}
