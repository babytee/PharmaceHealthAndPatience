package com.pharmacy.intelrx.pharmacy.repositories.auxilliary;

import com.pharmacy.intelrx.pharmacy.models.auxilliary.SalaryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalaryTypeRepository extends JpaRepository<SalaryType,Long> {
    Optional<SalaryType>findByName(String name);
}
