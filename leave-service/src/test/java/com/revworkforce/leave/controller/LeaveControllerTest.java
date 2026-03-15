package com.revworkforce.leave.controller;

import com.revworkforce.leave.dto.LeaveApplyRequest;
import com.revworkforce.leave.dto.LeaveDecisionRequest;
import com.revworkforce.leave.dto.LeaveResponse;
import com.revworkforce.leave.dto.LeaveSummaryResponse;
import com.revworkforce.leave.dto.TeamLeaveResponse;
import com.revworkforce.leave.enums.LeaveType;
import com.revworkforce.leave.service.LeaveService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveControllerTest {

    @Mock
    private LeaveService leaveService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private LeaveController controller;

    @Test
    void applyLeaveUsesHeaderUserId() {
        LeaveApplyRequest req = new LeaveApplyRequest();
        req.setLeaveType(LeaveType.CASUAL);
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now());
        req.setReason("Trip");

        when(httpRequest.getAttribute("userId")).thenReturn(null);
        when(httpRequest.getHeader("X-User-Id")).thenReturn("10");
        when(leaveService.applyLeave(10L, req)).thenReturn(new LeaveResponse());

        var res = controller.applyLeave(req, httpRequest);
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        verify(leaveService).applyLeave(10L, req);
    }

    @Test
    void getTeamLeavesDefaultsRoleToEmployee() {
        when(httpRequest.getAttribute("userId")).thenReturn("10");
        when(httpRequest.getAttribute("role")).thenReturn(null);
        when(httpRequest.getHeader("X-User-Role")).thenReturn(null);
        when(leaveService.getTeamLeaves(10L, "EMPLOYEE")).thenReturn(new TeamLeaveResponse());

        var res = controller.getTeamLeaves(httpRequest);
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    void getLeaveSummaryThrowsWhenNoUserId() {
        when(httpRequest.getAttribute("userId")).thenReturn(null);
        when(httpRequest.getHeader("X-User-Id")).thenReturn(null);
        assertThrows(RuntimeException.class, () -> controller.getLeaveSummary(httpRequest));
    }

    @Test
    void approveLeaveUsesRoleFromHeader() {
        LeaveDecisionRequest req = new LeaveDecisionRequest();
        req.setComment("ok");

        when(httpRequest.getAttribute("userId")).thenReturn(null);
        when(httpRequest.getHeader("X-User-Id")).thenReturn("10");
        when(httpRequest.getAttribute("role")).thenReturn(null);
        when(httpRequest.getHeader("X-User-Role")).thenReturn("ADMIN");

        when(leaveService.approveLeave(10L, 5L, req, "ADMIN")).thenReturn(new LeaveResponse());

        var res = controller.approveLeave(5L, req, httpRequest);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        verify(leaveService).approveLeave(10L, 5L, req, "ADMIN");
    }
}
