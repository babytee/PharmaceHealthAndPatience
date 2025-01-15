package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactInfoRepository extends JpaRepository<ContactInfo,Long> {
    Optional<ContactInfo>findByEmployeeId(Long employeeId);

    Optional<ContactInfo>findByPharmacyBranchId(Long pharmacyBranchId);
    Optional<ContactInfo> findByPharmacyId(Long pharmacyId);
}
