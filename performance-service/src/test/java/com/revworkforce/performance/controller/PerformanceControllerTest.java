package com.revworkforce.performance.controller;

import com.revworkforce.performance.dto.PerformanceReviewRequest;
import com.revworkforce.performance.dto.PerformanceReviewResponse;
import com.revworkforce.performance.dto.TeamReviewResponse;
import com.revworkforce.performance.service.PerformanceService;
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
class PerformanceControllerTest {

    @Mock
    private PerformanceService performanceService;

    @InjectMocks
    private PerformanceController controller;

    @Test
    void createReviewReturnsCreated() {
        PerformanceReviewRequest req = new PerformanceReviewRequest();
        req.setEmployeeId(11L);
        PerformanceReviewResponse res = new PerformanceReviewResponse();
        res.setId(1L);
        when(performanceService.createReview(10L, req, "ADMIN")).thenReturn(res);

        var response = controller.createReview(10L, "ADMIN", req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void getMyReviewsReturnsOk() {
        when(performanceService.getMyReviews(10L)).thenReturn(List.of(new PerformanceReviewResponse()));
        var response = controller.getMyReviews(10L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getTeamReviewsReturnsOk() {
        when(performanceService.getTeamReviews(10L, "ADMIN")).thenReturn(new TeamReviewResponse(List.of()));
        var response = controller.getTeamReviews(10L, "ADMIN");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
