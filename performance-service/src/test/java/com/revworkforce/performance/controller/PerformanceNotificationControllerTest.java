package com.revworkforce.performance.controller;

import com.revworkforce.performance.entity.PerformanceNotification;
import com.revworkforce.performance.service.PerformanceNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerformanceNotificationControllerTest {

    @Mock
    private PerformanceNotificationService notificationService;

    @InjectMocks
    private PerformanceNotificationController controller;

    @Test
    void getNotificationsReturnsOk() {
        when(notificationService.getNotifications(10L)).thenReturn(List.of(new PerformanceNotification()));
        var response = controller.getNotifications(10L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void markAsReadReturnsOk() {
        var response = controller.markAsRead(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

