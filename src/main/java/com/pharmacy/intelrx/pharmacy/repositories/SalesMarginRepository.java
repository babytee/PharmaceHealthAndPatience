package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.orgSettings.SalesMargin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalesMarginRepository extends JpaRepository<SalesMargin,Long> {
    Optional<SalesMargin>findByIntelRxId(String intelRxId);
}
