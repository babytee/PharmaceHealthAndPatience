package com.pharmacy.intelrx.brand.repositories;

import com.pharmacy.intelrx.brand.models.Brand;
import com.pharmacy.intelrx.brand.models.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand,Long> {
    Optional<Brand>findByName(String name);

    @Query("SELECT ms FROM Brand ms WHERE ms.id = :reqLabel OR ms.name = :reqLabel")
    Optional<Brand> findByIdOrName(@Param("reqLabel") String reqLabel);

    @Query("SELECT ms FROM Brand ms WHERE " +
            "(:name IS NULL OR ms.name LIKE %:name%)")
    Page<Brand> searchBrand(
            @Param("name") String name,
            Pageable pageable);
}
