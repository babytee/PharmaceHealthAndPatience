package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.PharmacistCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PharmacistCertificationRepository extends JpaRepository<PharmacistCertification,Long> {
   Optional<PharmacistCertification> findByCertificateNumber(String certificateNumber);
}
