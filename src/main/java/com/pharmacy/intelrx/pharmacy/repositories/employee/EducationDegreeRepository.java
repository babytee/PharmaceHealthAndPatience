package com.pharmacy.intelrx.pharmacy.repositories.employee;

import com.pharmacy.intelrx.pharmacy.models.employee.EducationDegree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EducationDegreeRepository extends JpaRepository<EducationDegree,Long> {
    List<EducationDegree>findAllByEducationId(Long educationId);

    Optional<EducationDegree> findByEducationId(Long educationId);
}
