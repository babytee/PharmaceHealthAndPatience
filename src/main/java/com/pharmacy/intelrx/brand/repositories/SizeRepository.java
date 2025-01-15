package com.pharmacy.intelrx.brand.repositories;

import com.pharmacy.intelrx.brand.models.BrandForm;
import com.pharmacy.intelrx.brand.models.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SizeRepository extends JpaRepository<Size,Long> {
    Optional<Size>findByName(String name);

    @Query("SELECT ms FROM Size ms WHERE " +
            "(:name IS NULL OR ms.name LIKE %:name%)")
    Page<Size> searchSize(
            @Param("name") String name,
            Pageable pageable);



}
