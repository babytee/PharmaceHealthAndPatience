package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.auxilliary.MedTestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedTestTypeRepository extends JpaRepository<MedTestType,Long> {
    Optional<MedTestType>findByTestTypeName(String name);
}
