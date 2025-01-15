package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.orgSettings.DrugExpirationNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DrugExpirationNotificationRepository extends JpaRepository<DrugExpirationNotification,Long> {
    Optional<DrugExpirationNotification>findByIntelRxId(String intelRxId);
}
