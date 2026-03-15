package com.revworkforce.admin.service;

import com.revworkforce.admin.client.EmployeeServiceClient;
import com.revworkforce.admin.dto.AdminNotificationResponse;
import com.revworkforce.admin.dto.NotificationSendRequest;
import com.revworkforce.admin.entity.AdminNotification;
import com.revworkforce.admin.exception.ResourceNotFoundException;
import com.revworkforce.admin.exception.UnauthorizedException;
import com.revworkforce.admin.repository.AdminNotificationRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminNotificationService {

    private static final Logger log = LoggerFactory.getLogger(AdminNotificationService.class);

    private final AdminNotificationRepository notificationRepository;
    private final EmployeeServiceClient employeeServiceClient;

    public AdminNotificationService(AdminNotificationRepository notificationRepository,
                                   EmployeeServiceClient employeeServiceClient) {
        this.notificationRepository = notificationRepository;
        this.employeeServiceClient = employeeServiceClient;
    }

    public List<AdminNotificationResponse> getNotifications(Long userId) {
        log.debug("Get notifications userId={}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminNotificationResponse sendNotification(Long senderUserId, String senderRole, NotificationSendRequest request) {
        log.info("Send notification senderUserId={} role={} sendToAll={}", senderUserId, senderRole, request.getSendToAll());
        if (Boolean.TRUE.equals(request.getSendToAll())) {
            if (!"ADMIN".equals(senderRole)) {
                throw new UnauthorizedException("Only administrators can broadcast notifications");
            }
            List<AdminNotificationResponse> responses = broadcastNotification(senderRole, request.getMessage());
            if (responses.isEmpty()) {
                throw new IllegalStateException("No notifications were created");
            }
            return responses.get(0);
        }

        Long userId = request.getRecipientUserId();
        if (userId == null) {
            throw new IllegalArgumentException("Recipient user ID is required");
        }

        AdminNotification notification = AdminNotification.builder()
                .userId(userId)
                .message(request.getMessage())
                .readFlag(false)
                .build();

        AdminNotification saved = notificationRepository.save(notification);
        log.info("Send notification success notificationId={}", saved.getId());
        return toResponse(saved);
    }
    @Transactional
    @CircuitBreaker(name = "employee-service", fallbackMethod = "broadcastNotificationFallback")
    public List<AdminNotificationResponse> broadcastNotification(String senderRole, String message) {
        log.info("Broadcast notification role={}", senderRole);
        if (!"ADMIN".equals(senderRole)) {
            throw new UnauthorizedException("Only administrators can broadcast notifications");
        }

        // Fetch all employees from employee service
        List<EmployeeServiceClient.EmployeeDto> employees = employeeServiceClient.getAllEmployees();

        List<AdminNotification> notifications = new ArrayList<>();
        for (EmployeeServiceClient.EmployeeDto employee : employees) {
            AdminNotification notification = AdminNotification.builder()
                    .userId(employee.getId())
                    .message(message)
                    .readFlag(false)
                    .build();
            notifications.add(notification);
        }

        List<AdminNotification> savedNotifications = notificationRepository.saveAll(notifications);
        log.info("Broadcast notification created count={}", savedNotifications.size());
        return savedNotifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AdminNotificationResponse> broadcastNotificationFallback(String senderRole, String message, Exception ex) {
        // Fallback: Just return empty list if employee service is down
        return new ArrayList<>();
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        log.debug("Mark notification read userId={} notificationId={}", userId, notificationId);
        AdminNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        // Ensure user can only mark their own notifications as read
        if (!notification.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only mark your own notifications as read");
        }

        notification.setReadFlag(true);
        notificationRepository.save(notification);
    }

    public long getUnreadCount(Long userId) {
        log.debug("Get unread count userId={}", userId);
        return notificationRepository.countByUserIdAndReadFlagFalse(userId);
    }

    private AdminNotificationResponse toResponse(AdminNotification notification) {
        return AdminNotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .readFlag(notification.isReadFlag())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
