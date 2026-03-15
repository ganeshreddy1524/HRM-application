package com.revworkforce.admin.controller;

import com.revworkforce.admin.dto.AdminNotificationResponse;
import com.revworkforce.admin.dto.NotificationSendRequest;
import com.revworkforce.admin.service.AdminNotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final AdminNotificationService notificationService;

    public NotificationController(AdminNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<AdminNotificationResponse>> getNotifications(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Get admin notifications userId={}", userId);
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @PostMapping("/send")
    public ResponseEntity<AdminNotificationResponse> sendNotification(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody NotificationSendRequest request) {
        log.info("Send admin notification senderUserId={} role={} sendToAll={}", userId, role, request.getSendToAll());
        AdminNotificationResponse response = notificationService.sendNotification(userId, role, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/broadcast")
    public ResponseEntity<List<AdminNotificationResponse>> broadcastNotification(
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, String> payload) {
        log.info("Broadcast admin notification role={}", role);
        String message = payload.get("message");
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required for broadcast");
        }
        List<AdminNotificationResponse> responses = notificationService.broadcastNotification(role, message);
        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        log.info("Mark admin notification read userId={} notificationId={}", userId, id);
        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestHeader("X-User-Id") Long userId) {
        log.info("Get admin unread notification count userId={}", userId);
        long count = notificationService.getUnreadCount(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
}
