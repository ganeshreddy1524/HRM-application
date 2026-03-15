package com.revworkforce.leave.service;

import com.revworkforce.leave.client.EmployeeServiceClient;
import com.revworkforce.leave.config.LeaveConfig;
import com.revworkforce.leave.dto.EmployeeDto;
import com.revworkforce.leave.dto.LeaveSummaryResponse;
import com.revworkforce.leave.entity.LeaveRequest;
import com.revworkforce.leave.enums.LeaveStatus;
import com.revworkforce.leave.enums.LeaveType;
import com.revworkforce.leave.repository.LeaveRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRepository;
    @Mock
    private EmployeeServiceClient employeeServiceClient;
    @Mock
    private LeaveNotificationService notificationService;
    @Mock
    private LeaveConfig leaveConfig;

    @InjectMocks
    private LeaveService leaveService;

    @Test
    void getLeaveSummaryCalculatesAllocatedUsedPendingAndRemaining() {
        int year = LocalDate.now().getYear();
        when(employeeServiceClient.getEmployeeByUserId(5L)).thenReturn(new EmployeeDto(7L, "Emp", 2L));
        when(leaveConfig.getTotalAllocatedLeaves()).thenReturn(24);
        when(leaveRepository.findByEmployeeId(7L)).thenReturn(List.of(
                new LeaveRequest(7L, LeaveType.PAID, LocalDate.of(year, 1, 10), LocalDate.of(year, 1, 11), "Trip", LeaveStatus.APPROVED),
                new LeaveRequest(7L, LeaveType.SICK, LocalDate.of(year, 2, 5), LocalDate.of(year, 2, 5), "Fever", LeaveStatus.PENDING)
        ));

        LeaveSummaryResponse response = leaveService.getLeaveSummary(5L);

        assertEquals(24, response.getTotalAllocated());
        assertEquals(2, response.getUsed());
        assertEquals(1, response.getPending());
        assertEquals(21, response.getRemaining());
    }
}
