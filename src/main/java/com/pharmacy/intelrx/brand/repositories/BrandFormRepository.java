package com.pharmacy.intelrx.brand.repositories;

import com.pharmacy.intelrx.brand.models.BrandClass;
import com.pharmacy.intelrx.brand.models.BrandForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandFormRepository extends JpaRepository<BrandForm,Long> {
    Optional<BrandForm>findByName(String name);

    List<BrandForm>findAllByBrandId(Long brandId);

    @Query("SELECT ms FROM BrandForm ms WHERE ms.id = :reqLabel OR ms.name = :reqLabel")
    Optional<BrandForm> findByIdOrName(@Param("reqLabel") String reqLabel);

    @Query("SELECT ms FROM BrandForm ms WHERE " +
            "(:name IS NULL OR ms.name LIKE %:name%)")
    Page<BrandForm> searchBrandForm(
            @Param("name") String name,
            Pageable pageable);

}
