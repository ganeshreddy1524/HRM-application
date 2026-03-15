package com.revworkforce.admin.controller;

import com.revworkforce.admin.dto.AdminNotificationResponse;
import com.revworkforce.admin.dto.NotificationSendRequest;
import com.revworkforce.admin.service.AdminNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private AdminNotificationService service;

    @InjectMocks
    private NotificationController controller;

    @Test
    void broadcastRequiresMessage() {
        assertThrows(IllegalArgumentException.class, () -> controller.broadcastNotification("ADMIN", Map.of()));
    }

    @Test
    void sendNotificationReturnsCreated() {
        NotificationSendRequest req = new NotificationSendRequest();
        req.setRecipientUserId(10L);
        req.setMessage("x");

        when(service.sendNotification(1L, "ADMIN", req)).thenReturn(AdminNotificationResponse.builder().id(2L).message("x").readFlag(false).build());
        var res = controller.sendNotification(1L, "ADMIN", req);
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertEquals(2L, res.getBody().getId());
    }

    @Test
    void markAsReadReturnsNoContent() {
        var res = controller.markAsRead(10L, 1L);
        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
        verify(service).markAsRead(1L, 10L);
    }

    @Test
    void getNotificationsReturnsOk() {
        when(service.getNotifications(10L)).thenReturn(List.of(AdminNotificationResponse.builder().id(1L).message("x").build()));
        var res = controller.getNotifications(10L);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
    }
}
