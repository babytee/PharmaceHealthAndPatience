package com.pharmacy.intelrx.pharmacy.repositories.auxilliary;

import com.pharmacy.intelrx.pharmacy.models.auxilliary.PaymentFrequency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentFrequencyRepository extends JpaRepository<PaymentFrequency,Long> {
    Optional<PaymentFrequency> findByName(String name);
}
