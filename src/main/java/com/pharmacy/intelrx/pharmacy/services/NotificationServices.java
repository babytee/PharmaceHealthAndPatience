package com.pharmacy.intelrx.pharmacy.services;

import com.pharmacy.intelrx.common.StandardResponse;
import com.pharmacy.intelrx.pharmacy.dto.NotificationEntityRequest;
import com.pharmacy.intelrx.pharmacy.dto.NotificationEntityResponse;
import com.pharmacy.intelrx.pharmacy.models.notification.NotificationEntity;
import com.pharmacy.intelrx.pharmacy.models.notification.NotificationType;
import com.pharmacy.intelrx.pharmacy.models.PharmacyBranch;
import com.pharmacy.intelrx.pharmacy.models.employee.Employee;
import com.pharmacy.intelrx.pharmacy.repositories.NotificationEntityRepository;
import com.pharmacy.intelrx.pharmacy.repositories.NotificationTypeRepository;
import com.pharmacy.intelrx.pharmacy.repositories.PharmacyBranchRepository;
import com.pharmacy.intelrx.pharmacy.repositories.employee.EmployeeRepository;
import com.pharmacy.intelrx.pharmacy.utility.FilterEmployee;
import com.pharmacy.intelrx.utility.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("NotificationServices")
public class NotificationServices {
    private final NotificationEntityRepository notificationEntityRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final PharmacyBranchRepository branchRepository;
    private final EmployeeRepository employeeRepository;
    private final FilterEmployee filterEmployee;
    private final UserDetailsService userDetailsService;

    public void submitNotification(NotificationEntityRequest request) {

        NotificationType notificationType = notificationTypeRepository.findById(request.getNotificationTypeId())
                .orElse(null);
        PharmacyBranch branch = null;
        if(request.getBranchId() != null) {
             branch = branchRepository.findById(request.getBranchId()).orElse(null);
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId()).orElse(null);

        NotificationEntity notificationEntity = NotificationEntity.builder()
                .notificationType(notificationType)
                .intelRxId(request.getIntelRxId())
                .branch(branch)
                .employee(employee)
                .notificationTitle(request.getNotificationTitle())
                .notificationMsg(request.getNotificationMsg())
                .notificationStatus(false)
                .notificationDateTime(LocalDateTime.now())
                .build();

        notificationEntityRepository.save(notificationEntity);

    }

    public ResponseEntity<?> getNotifications(Long notTypeId) {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }

        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        List<NotificationEntity> notificationEntityList = new ArrayList<>();
        if (notTypeId == null) {
            if (branch == null) {
                notificationEntityList = notificationEntityRepository.findAllByIntelRxIdAndBranch(
                        intelRxId, null);
            } else {
                notificationEntityList = notificationEntityRepository.findAllByIntelRxIdAndBranch(
                        intelRxId, branch);
            }

        } else {
            Optional<NotificationType> typeOptional = notificationTypeRepository.findById(notTypeId);
            if (!typeOptional.isPresent()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("notTypeId is not found"));
            }
            NotificationType notificationType = typeOptional.get();
            if (branch == null) {
                notificationEntityList = notificationEntityRepository.findAllByIntelRxIdAndNotificationTypeAndBranch(
                        intelRxId,
                        notificationType,
                        null
                );
            } else {
                notificationEntityList = notificationEntityRepository.findAllByIntelRxIdAndNotificationTypeAndBranch(
                        intelRxId,
                        notificationType,
                        branch
                );
            }

        }

        if (notificationEntityList.isEmpty()) {
            return ResponseEntity.ok(StandardResponse.success(notificationEntityList));
        }

        List<NotificationEntityResponse> responseList = notificationEntityList.stream().map(notificationEntity ->
                mapToNotifications(notificationEntity)).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    private NotificationEntityResponse mapToNotifications(NotificationEntity notificationEntity) {
        var notType = notificationEntity.getNotificationType();

        return NotificationEntityResponse.builder()
                .id(notificationEntity.getId())
                .intelRxId(notificationEntity.getIntelRxId())
                .notificationType(notType)
                .notificationTitle(notificationEntity.getNotificationTitle())
                .notificationMsg(notificationEntity.getNotificationMsg())
                .notificationStatus(notificationEntity.isNotificationStatus())
                .branchDetails(filterEmployee.mapToBranchResponse(notificationEntity.getEmployee()))
                .employeeDetails(filterEmployee.mapToUserInfo(notificationEntity.getEmployee()))
                .notificationDate(notificationEntity.getNotificationDateTime())
                .build();
    }

    public ResponseEntity<?> clearAllNotification() {
        var user = userDetailsService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.ok().body(StandardResponse.error("You are unauthorized"));
        }

        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        List<NotificationEntity> notificationEntityList = new ArrayList<>();

        if (branch == null) {
            notificationEntityList = notificationEntityRepository.findAllByIntelRxIdAndBranchAndNotificationStatus(
                    intelRxId, null, false);
        } else {
            notificationEntityList = notificationEntityRepository.findAllByIntelRxIdAndBranchAndNotificationStatus(
                    intelRxId, branch, false);
        }

        for (NotificationEntity notificationEntity : notificationEntityList) {
            notificationEntity.setNotificationStatus(true);
            notificationEntityRepository.save(notificationEntity);
        }

        return ResponseEntity.ok(StandardResponse.success("Cleared Successfully"));

    }

    public ResponseEntity<?> readNotification(Long notificationId) {
        var user = userDetailsService.getAuthenticatedUser();

        if (user == null) {
            return ResponseEntity.internalServerError().body(StandardResponse.error("You are unauthorized"));
        }

        if (notificationId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("notificationId not found"));
        }

        var intelRxId = userDetailsService.getIntelRxId();
        var branch = userDetailsService.getBranch();

        Optional<NotificationEntity> optionalNotification = null;

        if (branch == null) {
            optionalNotification = notificationEntityRepository.findByIdAndIntelRxIdAndBranchAndNotificationStatus(
                    notificationId, intelRxId, null, false);
        } else {
            optionalNotification = notificationEntityRepository.findByIdAndIntelRxIdAndBranchAndNotificationStatus(
                    notificationId, intelRxId, branch, false);
        }

        NotificationEntity notificationEntity = optionalNotification.get();

        notificationEntity.setNotificationStatus(true);
        notificationEntityRepository.save(notificationEntity);

        return ResponseEntity.ok(StandardResponse.success("Read Successfully"));

    }
}
