package com.pharmacy.intelrx.pharmacy.repositories.auxilliary;

import com.pharmacy.intelrx.pharmacy.models.auxilliary.RefillDuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefillDurationRepository extends JpaRepository<RefillDuration,Long> {
    Optional<RefillDuration>findByName(String name);
}
