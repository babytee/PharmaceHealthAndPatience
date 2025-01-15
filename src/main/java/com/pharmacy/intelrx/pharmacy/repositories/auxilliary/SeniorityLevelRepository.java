package com.pharmacy.intelrx.pharmacy.repositories.auxilliary;


import com.pharmacy.intelrx.pharmacy.models.auxilliary.SeniorityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeniorityLevelRepository extends JpaRepository<SeniorityLevel,Long> {
    Optional<SeniorityLevel> findByName(String name);
}
