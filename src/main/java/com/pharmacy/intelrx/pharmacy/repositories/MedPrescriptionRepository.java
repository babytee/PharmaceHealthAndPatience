package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.CartItem;
import com.pharmacy.intelrx.pharmacy.models.MedPrescription;
import com.pharmacy.intelrx.pharmacy.models.Patient;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedPrescriptionRepository extends JpaRepository<MedPrescription, Long> {
    Optional<MedPrescription> findByCartItem(CartItem cartItem);

    Optional<MedPrescription> findByIdAndIntelRxIdAndPharmacyBranchAndPatient(
            Long id, String intelRxId, PharmacyBranch pharmacyBranch, Patient patient
    );

    List<MedPrescription> findByIntelRxIdAndPharmacyBranchAndPatientAndRefill
            (String intelRxId, PharmacyBranch pharmacyBranch, Patient patient, boolean refillStatus);
}
