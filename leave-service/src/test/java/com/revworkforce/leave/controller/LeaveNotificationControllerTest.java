package com.revworkforce.leave.controller;

import com.revworkforce.leave.dto.LeaveNotificationResponse;
import com.revworkforce.leave.service.LeaveNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveNotificationControllerTest {

    @Mock
    private LeaveNotificationService service;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private LeaveNotificationController controller;

    @Test
    void getNotificationsUsesHeaderUserId() {
        when(httpRequest.getAttribute("userId")).thenReturn(null);
        when(httpRequest.getHeader("X-User-Id")).thenReturn("10");
        when(service.getNotifications(10L)).thenReturn(List.of(new LeaveNotificationResponse(1L, "m", false, null)));

        var res = controller.getNotifications(httpRequest);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
    }

    @Test
    void getUnreadCountThrowsWhenNoUserId() {
        when(httpRequest.getAttribute("userId")).thenReturn(null);
        when(httpRequest.getHeader("X-User-Id")).thenReturn(null);
        assertThrows(RuntimeException.class, () -> controller.getUnreadCount(httpRequest));
    }
}

