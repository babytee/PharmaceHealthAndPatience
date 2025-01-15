package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.MedTestResult;
import com.pharmacy.intelrx.pharmacy.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedTestResultRepository extends JpaRepository<MedTestResult, Long> {
    List<MedTestResult> findAllByIntelRxIdAndPatient(String intelRxId, Patient patient);


    Optional<MedTestResult> findByIdAndIntelRxId(Long id, String intelRxId);


}
