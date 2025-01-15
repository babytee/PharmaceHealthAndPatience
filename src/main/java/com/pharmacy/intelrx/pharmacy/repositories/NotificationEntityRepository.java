package com.pharmacy.intelrx.pharmacy.repositories;

import com.pharmacy.intelrx.pharmacy.models.notification.NotificationEntity;
import com.pharmacy.intelrx.pharmacy.models.notification.NotificationType;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationEntityRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findAllByIntelRxId(String intelRxId);

    List<NotificationEntity> findAllByIntelRxIdAndBranch(
            String intelRxId,
            PharmacyBranch branch
    );

    List<NotificationEntity> findAllByIntelRxIdAndNotificationTypeAndBranch(
            String intelRxId,
            NotificationType notificationType,
            PharmacyBranch branch
    );

    List<NotificationEntity> findAllByIntelRxIdAndBranchAndNotificationStatus(
            String intelRxId,
            PharmacyBranch branch,
            boolean status
    );


    Optional<NotificationEntity> findByIdAndIntelRxIdAndBranchAndNotificationStatus(
            Long id,
            String intelRxId,
            PharmacyBranch branch,
            boolean status
    );


}
