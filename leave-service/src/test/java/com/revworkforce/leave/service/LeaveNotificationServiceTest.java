package com.revworkforce.leave.service;

import com.revworkforce.leave.client.EmployeeServiceClient;
import com.revworkforce.leave.dto.EmployeeDto;
import com.revworkforce.leave.entity.LeaveNotification;
import com.revworkforce.leave.entity.LeaveRequest;
import com.revworkforce.leave.enums.LeaveStatus;
import com.revworkforce.leave.enums.LeaveType;
import com.revworkforce.leave.exception.ResourceNotFoundException;
import com.revworkforce.leave.repository.LeaveNotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveNotificationServiceTest {

    @Mock
    private LeaveNotificationRepository notificationRepository;
    @Mock
    private EmployeeServiceClient employeeServiceClient;

    @InjectMocks
    private LeaveNotificationService service;

    @Test
    void createNotificationSavesEntity() {
        when(notificationRepository.save(any(LeaveNotification.class))).thenAnswer(inv -> inv.getArgument(0));
        service.createNotification(10L, "hello");
        verify(notificationRepository).save(any(LeaveNotification.class));
    }

    @Test
    void getNotificationsMapsResponses() {
        LeaveNotification n = new LeaveNotification(10L, "m");
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(n));
        var res = service.getNotifications(10L);
        assertEquals(1, res.size());
        assertEquals("m", res.get(0).getMessage());
    }

    @Test
    void markAsReadThrowsWhenMissing() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.markAsRead(1L));
    }

    @Test
    void markAsReadUpdatesAndSaves() {
        LeaveNotification n = new LeaveNotification(10L, "m");
        n.setId(1L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(LeaveNotification.class))).thenAnswer(inv -> inv.getArgument(0));

        service.markAsRead(1L);
        verify(notificationRepository).save(any(LeaveNotification.class));
    }

    @Test
    void notifyLeaveSubmittedCreatesManagerNotificationWhenManagerExists() {
        LeaveRequest leave = new LeaveRequest(7L, LeaveType.PAID, LocalDate.now(), LocalDate.now(), "Trip", LeaveStatus.PENDING);
        leave.setId(99L);

        when(employeeServiceClient.getEmployee(7L)).thenReturn(new EmployeeDto(7L, 70L, "Emp", 3L));
        when(employeeServiceClient.getEmployee(3L)).thenReturn(new EmployeeDto(3L, 30L, "Mgr", null));
        when(notificationRepository.save(any(LeaveNotification.class))).thenAnswer(inv -> inv.getArgument(0));

        service.notifyLeaveSubmitted(leave);
        ArgumentCaptor<LeaveNotification> captor = ArgumentCaptor.forClass(LeaveNotification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(30L, captor.getValue().getUserId());
    }

    @Test
    void notifyLeaveApprovedDoesNotThrowWhenEmployeeServiceFails() {
        LeaveRequest leave = new LeaveRequest(7L, LeaveType.PAID, LocalDate.now(), LocalDate.now(), "Trip", LeaveStatus.APPROVED);
        leave.setId(99L);
        when(employeeServiceClient.getEmployee(7L)).thenThrow(new RuntimeException("down"));
        service.notifyLeaveApproved(leave);
    }

    @Test
    void notifyLeaveApprovedUsesEmployeeUserIdWhenPresent() {
        LeaveRequest leave = new LeaveRequest(7L, LeaveType.PAID, LocalDate.now(), LocalDate.now(), "Trip", LeaveStatus.APPROVED);
        leave.setId(99L);

        when(employeeServiceClient.getEmployee(7L)).thenReturn(new EmployeeDto(7L, 70L, "Emp", 3L));
        when(notificationRepository.save(any(LeaveNotification.class))).thenAnswer(inv -> inv.getArgument(0));

        service.notifyLeaveApproved(leave);

        ArgumentCaptor<LeaveNotification> captor = ArgumentCaptor.forClass(LeaveNotification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(70L, captor.getValue().getUserId());
        assertTrue(captor.getValue().getMessage().toLowerCase().contains("approved"));
    }
}
