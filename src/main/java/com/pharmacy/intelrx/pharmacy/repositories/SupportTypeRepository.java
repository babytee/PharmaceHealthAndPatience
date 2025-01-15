package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.support.SupportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupportTypeRepository extends JpaRepository<SupportType,Long> {
    Optional<SupportType>findByName(String name);
}
