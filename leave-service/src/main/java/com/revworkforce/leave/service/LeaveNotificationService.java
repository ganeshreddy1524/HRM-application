package com.revworkforce.leave.service;

import com.revworkforce.leave.client.EmployeeServiceClient;
import com.revworkforce.leave.dto.EmployeeDto;
import com.revworkforce.leave.dto.LeaveNotificationResponse;
import com.revworkforce.leave.entity.LeaveNotification;
import com.revworkforce.leave.entity.LeaveRequest;
import com.revworkforce.leave.exception.ResourceNotFoundException;
import com.revworkforce.leave.repository.LeaveNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveNotificationService {

    private static final Logger log = LoggerFactory.getLogger(LeaveNotificationService.class);

    private final LeaveNotificationRepository notificationRepository;
    private final EmployeeServiceClient employeeServiceClient;

    public LeaveNotificationService(LeaveNotificationRepository notificationRepository,
                                   EmployeeServiceClient employeeServiceClient) {
        this.notificationRepository = notificationRepository;
        this.employeeServiceClient = employeeServiceClient;
    }

    @Transactional
    public void createNotification(Long userId, String message) {
        log.debug("Create leave notification userId={}", userId);
        LeaveNotification notification = new LeaveNotification(userId, message);
        notificationRepository.save(notification);
    }

    public List<LeaveNotificationResponse> getNotifications(Long userId) {
        log.debug("Get leave notifications userId={}", userId);
        List<LeaveNotification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        log.debug("Mark leave notification read notificationId={}", notificationId);
        LeaveNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notification.setReadFlag(true);
        notificationRepository.save(notification);
    }

    public long getUnreadCount(Long userId) {
        log.debug("Get leave unread count userId={}", userId);
        return notificationRepository.countByUserIdAndReadFlagFalse(userId);
    }

    @Transactional
    public void notifyLeaveSubmitted(LeaveRequest leave) {
        try {
            // Get employee details
            EmployeeDto employee = employeeServiceClient.getEmployee(leave.getEmployeeId());

            if (employee.getManagerId() != null) {
                // Notify manager about new leave request
                String message = String.format("New leave request from %s for %s (%s to %s)",
                        employee.getFullName(),
                        leave.getLeaveType(),
                        leave.getStartDate(),
                        leave.getEndDate());

                // Get manager's user ID
                EmployeeDto manager = employeeServiceClient.getEmployee(employee.getManagerId());
                if (manager.getUserId() != null) {
                    createNotification(manager.getUserId(), message);
                }
            }
        } catch (Exception e) {
            // Don't fail the transaction if notification delivery fails.
            log.warn("Failed to send leave submission notification leaveId={}", leave.getId(), e);
        }
    }

    @Transactional
    public void notifyLeaveApproved(LeaveRequest leave) {
        try {
            EmployeeDto employee = employeeServiceClient.getEmployee(leave.getEmployeeId());

            String message = String.format("Your leave request for %s (%s to %s) has been approved",
                    leave.getLeaveType(),
                    leave.getStartDate(),
                    leave.getEndDate());

            if (employee.getUserId() != null) {
                createNotification(employee.getUserId(), message);
            }
        } catch (Exception e) {
            log.warn("Failed to send leave approval notification leaveId={}", leave.getId(), e);
        }
    }

    @Transactional
    public void notifyLeaveRejected(LeaveRequest leave) {
        try {
            EmployeeDto employee = employeeServiceClient.getEmployee(leave.getEmployeeId());

            String message = String.format("Your leave request for %s (%s to %s) has been rejected. Reason: %s",
                    leave.getLeaveType(),
                    leave.getStartDate(),
                    leave.getEndDate(),
                    leave.getManagerComment() != null ? leave.getManagerComment() : "No reason provided");

            if (employee.getUserId() != null) {
                createNotification(employee.getUserId(), message);
            }
        } catch (Exception e) {
            log.warn("Failed to send leave rejection notification leaveId={}", leave.getId(), e);
        }
    }

    private LeaveNotificationResponse mapToResponse(LeaveNotification notification) {
        return new LeaveNotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.isReadFlag(),
                notification.getCreatedAt()
        );
    }
}
