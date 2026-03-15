package com.revworkforce.performance.service;

import com.revworkforce.performance.client.EmployeeServiceClient;
import com.revworkforce.performance.dto.EmployeeDto;
import com.revworkforce.performance.dto.GoalCommentRequest;
import com.revworkforce.performance.dto.GoalRequest;
import com.revworkforce.performance.dto.GoalResponse;
import com.revworkforce.performance.dto.GoalStatusUpdateRequest;
import com.revworkforce.performance.entity.Goal;
import com.revworkforce.performance.enums.GoalPriority;
import com.revworkforce.performance.enums.GoalStatus;
import com.revworkforce.performance.exception.ResourceNotFoundException;
import com.revworkforce.performance.exception.UnauthorizedException;
import com.revworkforce.performance.repository.GoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;
    @Mock
    private EmployeeServiceClient employeeServiceClient;
    @Mock
    private PerformanceNotificationService notificationService;

    @InjectMocks
    private GoalService goalService;

    @Test
    void createGoalUsesEmployeeResolvedFromUserId() {
        GoalRequest request = new GoalRequest();
        request.setDescription("Finish sprint goals");
        request.setDeadline(LocalDate.now().plusDays(10));
        request.setPriority(GoalPriority.HIGH);

        Goal savedGoal = new Goal();
        savedGoal.setId(3L);
        savedGoal.setEmployeeId(11L);
        savedGoal.setDescription(request.getDescription());
        savedGoal.setDeadline(request.getDeadline());
        savedGoal.setPriority(request.getPriority());

        when(employeeServiceClient.getEmployeeByUserId(9L)).thenReturn(new EmployeeDto(11L, "Goal User", 2L));
        when(employeeServiceClient.getEmployee(11L)).thenReturn(new EmployeeDto(11L, "Goal User", 2L));
        when(goalRepository.save(any(Goal.class))).thenReturn(savedGoal);

        GoalResponse response = goalService.createGoal(9L, request);

        assertEquals(3L, response.getId());
        assertEquals(11L, response.getEmployeeId());
        assertEquals("Goal User", response.getEmployeeName());
        assertEquals("Finish sprint goals", response.getDescription());
    }

    @Test
    void getMyGoalsThrowsWhenEmployeeNotFound() {
        when(employeeServiceClient.getEmployeeByUserId(1L)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> goalService.getMyGoals(1L));
    }

    @Test
    void getMyGoalsMapsEmployeeNameAndHandlesEmployeeLookupFailure() {
        when(employeeServiceClient.getEmployeeByUserId(1L)).thenReturn(new EmployeeDto(11L, "User", 2L));

        Goal goal = new Goal();
        goal.setId(10L);
        goal.setEmployeeId(11L);
        goal.setDescription("D1");
        goal.setPriority(GoalPriority.LOW);
        goal.setStatus(GoalStatus.NOT_STARTED);
        when(goalRepository.findByEmployeeId(11L)).thenReturn(List.of(goal));

        doThrow(new RuntimeException("down")).when(employeeServiceClient).getEmployee(11L);

        List<GoalResponse> responses = goalService.getMyGoals(1L);
        assertEquals(1, responses.size());
        assertEquals("Unknown", responses.get(0).getEmployeeName());
    }

    @Test
    void getTeamGoalsRejectsEmployeeRole() {
        assertThrows(UnauthorizedException.class, () -> goalService.getTeamGoals(1L, "EMPLOYEE"));
    }

    @Test
    void updateGoalStatusUpdatesWhenOwnerMatches() {
        Goal existing = new Goal();
        existing.setId(5L);
        existing.setEmployeeId(11L);
        existing.setDescription("D");
        existing.setPriority(GoalPriority.MEDIUM);
        existing.setStatus(GoalStatus.NOT_STARTED);

        when(goalRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(employeeServiceClient.getEmployeeByUserId(1L)).thenReturn(new EmployeeDto(11L, "Owner", 2L));
        when(employeeServiceClient.getEmployee(11L)).thenReturn(new EmployeeDto(11L, "Owner", 2L));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));

        GoalStatusUpdateRequest req = new GoalStatusUpdateRequest();
        req.setStatus(GoalStatus.IN_PROGRESS);

        GoalResponse response = goalService.updateGoalStatus(1L, 5L, req);
        assertEquals(GoalStatus.IN_PROGRESS, response.getStatus());
        assertEquals("Owner", response.getEmployeeName());
    }

    @Test
    void updateGoalStatusRejectsWhenNotOwner() {
        Goal existing = new Goal();
        existing.setId(5L);
        existing.setEmployeeId(11L);
        when(goalRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(employeeServiceClient.getEmployeeByUserId(1L)).thenReturn(new EmployeeDto(99L, "Other", 2L));

        GoalStatusUpdateRequest req = new GoalStatusUpdateRequest();
        req.setStatus(GoalStatus.IN_PROGRESS);

        assertThrows(UnauthorizedException.class, () -> goalService.updateGoalStatus(1L, 5L, req));
    }

    @Test
    void addManagerCommentRejectsWhenNotManagerOrAdmin() {
        GoalCommentRequest req = new GoalCommentRequest();
        req.setManagerComment("x");
        assertThrows(UnauthorizedException.class, () -> goalService.addManagerComment(1L, 2L, req, "EMPLOYEE"));
    }

    @Test
    void addManagerCommentSavesAndNotifies() {
        Goal existing = new Goal();
        existing.setId(2L);
        existing.setEmployeeId(11L);
        when(goalRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));

        GoalCommentRequest req = new GoalCommentRequest();
        req.setManagerComment("Good job");

        goalService.addManagerComment(1L, 2L, req, "MANAGER");

        verify(notificationService).notifyGoalCommented(any(Goal.class));
        verify(goalRepository).save(any(Goal.class));
    }
}
