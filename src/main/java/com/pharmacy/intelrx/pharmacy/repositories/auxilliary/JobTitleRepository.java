package com.pharmacy.intelrx.pharmacy.repositories.auxilliary;

import com.pharmacy.intelrx.pharmacy.models.auxilliary.JobTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobTitleRepository extends JpaRepository<JobTitle,Long> {
    Optional<JobTitle>findByName(String name);

    List<JobTitle> findAllByName(String name);

    List<JobTitle> findAllById(int i);
}
