package com.pharmacy.intelrx.pharmacy.controllers;

import com.pharmacy.intelrx.pharmacy.services.NotificationServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping({"/api/v1/user/notification"})
@RestController("NotificationController")
public class NotificationController {
    private final NotificationServices notificationServices;

    @GetMapping()
    public ResponseEntity<?> getNotifications(
            @RequestParam(required = false,
                    name = "notTypeId") Long notTypeId) {
        return notificationServices.getNotifications(notTypeId);
    }

    @GetMapping({"clear_all"})
    public ResponseEntity<?> clearAllNotification() {
        return notificationServices.clearAllNotification();
    }

    @GetMapping({"read"})
    public ResponseEntity<?> readNotification(
            @RequestParam(name = "notificationId", required = true) Long notificationId) {
        return notificationServices.readNotification(notificationId);
    }


}
