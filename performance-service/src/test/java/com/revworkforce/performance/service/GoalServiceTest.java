package com.revworkforce.performance.service;

import com.revworkforce.performance.client.EmployeeServiceClient;
import com.revworkforce.performance.dto.EmployeeDto;
import com.revworkforce.performance.dto.GoalRequest;
import com.revworkforce.performance.dto.GoalResponse;
import com.revworkforce.performance.entity.Goal;
import com.revworkforce.performance.enums.GoalPriority;
import com.revworkforce.performance.repository.GoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
}
