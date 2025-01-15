package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.orgSettings.OutOfStockNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OutOfStockNotificationRepository extends JpaRepository<OutOfStockNotification,Long> {
    Optional<OutOfStockNotification>findByIntelRxId(String intelRxId);
}
