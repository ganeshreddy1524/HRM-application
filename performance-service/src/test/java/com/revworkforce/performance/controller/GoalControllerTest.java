package com.revworkforce.performance.controller;

import com.revworkforce.performance.dto.GoalCommentRequest;
import com.revworkforce.performance.dto.GoalRequest;
import com.revworkforce.performance.dto.GoalResponse;
import com.revworkforce.performance.dto.GoalStatusUpdateRequest;
import com.revworkforce.performance.dto.TeamGoalResponse;
import com.revworkforce.performance.enums.GoalPriority;
import com.revworkforce.performance.enums.GoalStatus;
import com.revworkforce.performance.service.GoalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalControllerTest {

    @Mock
    private GoalService goalService;

    @InjectMocks
    private GoalController controller;

    @Test
    void createGoalReturnsCreated() {
        GoalRequest req = new GoalRequest();
        req.setDescription("D");
        req.setDeadline(LocalDate.now().plusDays(1));
        req.setPriority(GoalPriority.HIGH);

        GoalResponse res = new GoalResponse();
        res.setId(1L);
        when(goalService.createGoal(10L, req)).thenReturn(res);

        var response = controller.createGoal(10L, req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void getMyGoalsReturnsOk() {
        when(goalService.getMyGoals(10L)).thenReturn(List.of(new GoalResponse()));
        var response = controller.getMyGoals(10L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getTeamGoalsReturnsOk() {
        when(goalService.getTeamGoals(10L, "ADMIN")).thenReturn(new TeamGoalResponse(List.of()));
        var response = controller.getTeamGoals(10L, "ADMIN");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateGoalStatusReturnsOk() {
        GoalStatusUpdateRequest req = new GoalStatusUpdateRequest();
        req.setStatus(GoalStatus.COMPLETED);
        GoalResponse res = new GoalResponse();
        res.setStatus(GoalStatus.COMPLETED);
        when(goalService.updateGoalStatus(10L, 5L, req)).thenReturn(res);

        var response = controller.updateGoalStatus(10L, 5L, req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(GoalStatus.COMPLETED, response.getBody().getStatus());
    }

    @Test
    void addManagerCommentReturnsOk() {
        GoalCommentRequest req = new GoalCommentRequest();
        req.setManagerComment("x");
        when(goalService.addManagerComment(10L, 5L, req, "MANAGER")).thenReturn(new GoalResponse());
        var response = controller.addManagerComment(10L, "MANAGER", 5L, req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

