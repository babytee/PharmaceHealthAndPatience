package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("SELECT p FROM Patient p WHERE " +
            "p.gender <> 'Unknown' AND " +
            "p.phoneNumber <> 'Unknown' AND " +
            "((LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(LOWER(p.gender) LIKE CONCAT('%', :keyword, '%')) OR " +
            "(LOWER(p.phoneNumber) LIKE CONCAT('%', :keyword, '%'))) OR " +
            "(:keyword IS NULL)")
    List<Patient> searchPatients(
            @Param("keyword") String keyword);

    @Query("SELECT p FROM Patient p WHERE " +
            "(:intelRxId IS NULL OR p.intelRxId = :intelRxId) AND " +
            "p.gender <> 'Unknown' AND " +
            "p.phoneNumber <> 'Unknown' AND " +
            "((:patientSearch IS NULL) OR " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :patientSearch, '%'))) OR " +
            "(LOWER(p.gender) LIKE LOWER(CONCAT('%', :patientSearch, '%'))) OR " +
            "(LOWER(p.phoneNumber) LIKE LOWER(CONCAT('%', :patientSearch, '%'))))")
    Page<Patient> searchPharmacyPatients(
            @Param("intelRxId") String intelRxId,
            @Param("patientSearch") String patientSearch,
            Pageable pageable);



    @Query("SELECT p FROM Patient p " +
            "WHERE (:keyword IS NULL OR " +
            "p.gender <> 'Unknown' AND " +
            "p.phoneNumber <> 'Unknown' AND " +
            "LOWER(p.name) LIKE CONCAT('%', LOWER(:keyword), '%') OR " +
            "LOWER(p.gender) LIKE CONCAT('%', LOWER(:keyword), '%') OR " +
            "LOWER(p.phoneNumber) LIKE CONCAT('%', LOWER(:keyword), '%')) " +
            "OR p.intelRxId IN (SELECT ph.intelRxId FROM Pharmacy ph " +
            "WHERE (:keyword IS NULL OR " +
            "LOWER(ph.pharmacyName) LIKE CONCAT('%', LOWER(:keyword), '%') OR " +
            "LOWER(ph.premiseNumber) LIKE CONCAT('%', LOWER(:keyword), '%'))) " +
            "OR EXISTS (SELECT 1 FROM ContactInfo ci " +
            "JOIN ci.pharmacy ph2 " +
            "WHERE ph2.intelRxId = p.intelRxId " +
            "AND (:keyword IS NULL OR " +
            "LOWER(ci.country) LIKE CONCAT('%', LOWER(:keyword), '%') OR " +
            "LOWER(ci.state) LIKE CONCAT('%', LOWER(:keyword), '%') OR " +
            "LOWER(ci.city) LIKE CONCAT('%', LOWER(:keyword), '%')))")
    Page<Patient> searchAllPatient(@Param("keyword") String keyword, Pageable pageable);


    @Query("SELECT p FROM Patient p WHERE " +
            "(:intelRxId IS NULL OR p.intelRxId = :intelRxId) AND " +
            "(:branchId IS NULL OR p.pharmacyBranch.id = :branchId) AND " +
            "p.gender <> 'Unknown' AND " +
            "p.phoneNumber <> 'Unknown' AND " +
            "(:searchTerm IS NULL OR " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.gender) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.phoneNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))))")
    Page<Patient> findByPatientsFilter(
            @Param("intelRxId") String intelRxId,
            @Param("branchId") Long branchId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);



    List<Patient> findAllByIntelRxId(String intelRxId);

    Optional<Patient> findByIdAndIntelRxId(Long patientId, String intelRxId);

    int countByIntelRxIdAndCreatedAt(String intelRxId, LocalDateTime period);
    int countByIntelRxIdAndCreatedAtBetween(String intelRxId, LocalDateTime startDate, LocalDateTime endDate);


}
