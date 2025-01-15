package com.pharmacy.intelrx.pharmacy.repositories.auxilliary;


import com.pharmacy.intelrx.pharmacy.models.auxilliary.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule,Long> {
    Optional<WorkSchedule>findByName(String name);
}
