package com.pharmacy.intelrx.auxilliary.repositories;

import com.pharmacy.intelrx.pharmacy.models.auxilliary.KPIType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KPITypeRepository extends JpaRepository<KPIType,Long> {
    Optional<KPIType>findByRecommendationName(String name);
}
