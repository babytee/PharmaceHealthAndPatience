package com.pharmacy.intelrx.pharmacy.utility;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.models.PharmacyWallet;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component("PharmacyWalletConfig")
public class PharmacyWalletConfig {
    private final PharmacyWalletRepository pharmacyWalletRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createWallet(String intelRxId) {
        var pharmacyWallet = pharmacyWalletRepository.findByIntelRxId(intelRxId).orElse(null);
        if (pharmacyWallet == null) {
            PharmacyWallet wallet = new PharmacyWallet();
            wallet.setBalance(0.00);
            wallet.setCreatedAt(LocalDateTime.now());
            wallet.setIntelRxId(intelRxId);
            pharmacyWalletRepository.save(wallet);
        }
    }

    public void creditWallet(String intelRxId, double amount) {
        var wallet = pharmacyWalletRepository.findByIntelRxId(intelRxId).orElse(null);
        if (wallet != null) {
            wallet.setBalance(wallet.getBalance() + amount);
            pharmacyWalletRepository.save(wallet);
        }
    }

    public void debitWallet(String intelRxId, Double amount) {
        var wallet = pharmacyWalletRepository.findByIntelRxId(intelRxId).orElse(null);
        if (wallet != null && wallet.getBalance() >= amount) {
            wallet.setBalance(wallet.getBalance() - amount);
            pharmacyWalletRepository.save(wallet);
        } else {
            if (wallet == null) {
                throw new IllegalArgumentException("Wallet not found for the given intelRxId: " + intelRxId);
            } else {
                throw new IllegalArgumentException("Insufficient wallet balance. Current balance is: " + wallet.getBalance() + " for this transaction.");
            }
        }
    }

}
