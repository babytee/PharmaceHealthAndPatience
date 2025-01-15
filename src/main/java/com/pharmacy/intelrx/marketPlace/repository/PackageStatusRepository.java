package com.pharmacy.intelrx.marketPlace.repository;

import com.pharmacy.intelrx.marketPlace.models.PackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageStatusRepository extends JpaRepository<PackageStatus, Long> {
    List<PackageStatus> findByIntelRxIdAndCartItemId
            (String intelRxId, Long cartItemId);

    Optional<PackageStatus> findByIntelRxIdAndDeliveryStatusAndCartItemId
            (String intelRxId,String deliveryStatus, Long cartItemId);
}
