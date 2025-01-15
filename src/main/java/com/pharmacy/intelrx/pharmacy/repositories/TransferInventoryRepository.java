package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.TransferInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransferInventoryRepository extends JpaRepository<TransferInventory, Long> {
    Page<TransferInventory> findAllByIntelRxId(String intelRxId, Pageable pageable);

    List<TransferInventory> findAllByIntelRxIdAndTransferTo(String intelRxId, PharmacyBranch transferTo);

    Page<TransferInventory> findByIntelRxIdAndTransferTo(String intelRxId, PharmacyBranch transferTo, Pageable pageable);


    Optional<TransferInventory> findByIntelRxIdAndId(String intelRxId, Long id);


}
