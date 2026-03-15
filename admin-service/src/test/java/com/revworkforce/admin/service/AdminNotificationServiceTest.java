package com.revworkforce.admin.service;

import com.revworkforce.admin.client.EmployeeServiceClient;
import com.revworkforce.admin.dto.NotificationSendRequest;
import com.revworkforce.admin.entity.AdminNotification;
import com.revworkforce.admin.exception.UnauthorizedException;
import com.revworkforce.admin.repository.AdminNotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminNotificationServiceTest {

    @Mock
    private AdminNotificationRepository notificationRepository;

    @Mock
    private EmployeeServiceClient employeeServiceClient;

    @InjectMocks
    private AdminNotificationService service;

    @Test
    void sendNotificationRequiresRecipientWhenNotBroadcast() {
        NotificationSendRequest req = new NotificationSendRequest();
        req.setMessage("hello");
        assertThrows(IllegalArgumentException.class, () -> service.sendNotification(1L, "ADMIN", req));
    }

    @Test
    void sendNotificationBroadcastRequiresAdminRole() {
        NotificationSendRequest req = new NotificationSendRequest();
        req.setSendToAll(true);
        req.setMessage("hello");
        assertThrows(UnauthorizedException.class, () -> service.sendNotification(1L, "EMPLOYEE", req));
    }

    @Test
    void sendNotificationSavesSingleNotification() {
        NotificationSendRequest req = new NotificationSendRequest();
        req.setRecipientUserId(10L);
        req.setMessage("hello");

        AdminNotification saved = AdminNotification.builder()
                .id(5L)
                .userId(10L)
                .message("hello")
                .readFlag(false)
                .build();
        when(notificationRepository.save(any(AdminNotification.class))).thenReturn(saved);

        var res = service.sendNotification(1L, "ADMIN", req);
        assertEquals(5L, res.getId());
        assertEquals("hello", res.getMessage());
    }

    @Test
    void broadcastNotificationRequiresAdminRole() {
        assertThrows(UnauthorizedException.class, () -> service.broadcastNotification("MANAGER", "x"));
    }

    @Test
    void broadcastNotificationCreatesOnePerEmployee() {
        when(employeeServiceClient.getAllEmployees()).thenReturn(List.of(
                new EmployeeServiceClient.EmployeeDto(1L, "a@b.com", "A", "B"),
                new EmployeeServiceClient.EmployeeDto(2L, "c@d.com", "C", "D")
        ));
        when(notificationRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        var res = service.broadcastNotification("ADMIN", "msg");
        assertEquals(2, res.size());
        assertEquals("msg", res.get(0).getMessage());
    }

    @Test
    void markAsReadRejectsOtherUsers() {
        AdminNotification n = AdminNotification.builder().id(1L).userId(10L).message("x").readFlag(false).build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));
        assertThrows(UnauthorizedException.class, () -> service.markAsRead(1L, 11L));
    }

    @Test
    void markAsReadUpdatesFlagAndSaves() {
        AdminNotification n = AdminNotification.builder().id(1L).userId(10L).message("x").readFlag(false).build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(AdminNotification.class))).thenReturn(n);

        service.markAsRead(1L, 10L);
        verify(notificationRepository).save(any(AdminNotification.class));
    }

    @Test
    void getUnreadCountDelegatesToRepository() {
        when(notificationRepository.countByUserIdAndReadFlagFalse(10L)).thenReturn(3L);
        assertEquals(3L, service.getUnreadCount(10L));
    }
}

