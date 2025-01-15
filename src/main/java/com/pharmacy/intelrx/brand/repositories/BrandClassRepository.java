package com.pharmacy.intelrx.brand.repositories;

import com.pharmacy.intelrx.auxilliary.models.InventoryDictionary;
import com.pharmacy.intelrx.brand.models.BrandClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandClassRepository extends JpaRepository<BrandClass,Long> {
    Optional<BrandClass>findByName(String name);
    List<BrandClass>findAllByBrandId(Long brandId);

    @Query("SELECT ms FROM BrandClass ms WHERE ms.id = :reqLabel OR ms.name = :reqLabel")
    Optional<BrandClass> findByIdOrName(@Param("reqLabel") String reqLabel);

    @Query("SELECT ms FROM BrandClass ms WHERE " +
            "(:name IS NULL OR ms.name LIKE %:name%)")
    Page<BrandClass> searchBrandClass(
            @Param("name") String name,
            Pageable pageable);

}
