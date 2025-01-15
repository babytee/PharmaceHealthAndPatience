package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.PharmacyWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PharmacyWalletRepository extends JpaRepository<PharmacyWallet,Long> {
    Optional<PharmacyWallet>findByIntelRxId(String intelRxId);
}
