package com.pharmacy.intelrx.auxilliary.repositories;

import com.pharmacy.intelrx.auxilliary.models.InventoryDictionary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryDictionaryRepository extends JpaRepository<InventoryDictionary, Long> {
    List<InventoryDictionary> findAllByInventoryType(String inventoryType);

    Optional<InventoryDictionary> findByItemName(String itemName);

    // Custom query to support search filter based on itemName or inventoryType with pagination
    @Query("SELECT i FROM InventoryDictionary i WHERE " +
            "(:itemName IS NULL OR i.itemName LIKE %:itemName%) AND " +
            "(:inventoryType IS NULL OR i.inventoryType = :inventoryType)")
    Page<InventoryDictionary> searchInventory(
            @Param("itemName") String itemName,
            @Param("inventoryType") String inventoryType,
            Pageable pageable);
}
