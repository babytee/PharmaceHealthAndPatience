package com.pharmacy.intelrx.pharmacy.repositories.auxilliary;

import com.pharmacy.intelrx.pharmacy.models.auxilliary.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType,Long> {
    Optional<DocumentType>findByName(String name);
}
