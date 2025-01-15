package com.pharmacy.intelrx.brand.repositories;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.brand.models.PharmaceuticalBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PharmaceuticalBrandRepository extends JpaRepository<PharmaceuticalBrand, Long> {
    Optional<PharmaceuticalBrand> findByUser(User user);
}
