package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyBranchRepository extends JpaRepository<PharmacyBranch, Long> {
    Optional<PharmacyBranch> findByName(String name);

    Optional<PharmacyBranch> findByIntelRxIdAndName(String intelRxId, String name);

    Optional<PharmacyBranch> findByIdAndIntelRxId(Long id, String intelRxId);

    Optional<PharmacyBranch> findByIdAndIntelRxIdAndEmployeeId(Long id, String intelRxId,Long employeeId);

    Optional<PharmacyBranch> findByUserId(Long userId);

    Optional<PharmacyBranch> findByEmployeeId(Long employeeId);

    List<PharmacyBranch> findAllByUserId(Long userId);

    List<PharmacyBranch> findAllByIntelRxId(String intelRxId);


}
