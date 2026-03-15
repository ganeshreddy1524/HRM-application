package com.revworkforce.performance.controller;

import com.revworkforce.performance.entity.PerformanceNotification;
import com.revworkforce.performance.service.PerformanceNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/performance/notifications")
public class PerformanceNotificationController {

    private static final Logger log = LoggerFactory.getLogger(PerformanceNotificationController.class);

    private final PerformanceNotificationService notificationService;

    @Autowired
    public PerformanceNotificationController(PerformanceNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<PerformanceNotification>> getNotifications(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Get performance notifications userId={}", userId);
        List<PerformanceNotification> notifications = notificationService.getNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        log.info("Mark performance notification read notificationId={}", id);
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
