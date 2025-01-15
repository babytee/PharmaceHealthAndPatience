package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.KPIRecommendation;
import com.pharmacy.intelrx.pharmacy.models.auxilliary.KPIType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KPIRecommendationRepository extends JpaRepository<KPIRecommendation,Long> {
    Optional<KPIRecommendation>findByIdAndIntelRxId(Long id, String intelRxId);

    List<KPIRecommendation> findAllByIntelRxId(String intelRxId);

    List<KPIRecommendation> findAllByIntelRxIdAndKpiType(String intelRxId, KPIType kpiType);

    Optional<KPIRecommendation> findByIntelRxIdAndKpiTypeId(String intelRxId, Long kpiTypeId);

}
