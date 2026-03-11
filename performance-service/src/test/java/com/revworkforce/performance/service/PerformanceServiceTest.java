package com.revworkforce.performance.service;

import com.revworkforce.performance.client.EmployeeServiceClient;
import com.revworkforce.performance.dto.EmployeeDto;
import com.revworkforce.performance.dto.TeamReviewResponse;
import com.revworkforce.performance.entity.PerformanceReview;
import com.revworkforce.performance.enums.ReviewStatus;
import com.revworkforce.performance.repository.PerformanceReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

    @Mock
    private PerformanceReviewRepository reviewRepository;
    @Mock
    private EmployeeServiceClient employeeServiceClient;
    @Mock
    private PerformanceNotificationService notificationService;

    @InjectMocks
    private PerformanceService performanceService;

    @Test
    void getTeamReviewsForAdminReturnsAllReviews() {
        PerformanceReview review = new PerformanceReview();
        review.setId(100L);
        review.setEmployeeId(11L);
        review.setReviewerId(22L);
        review.setStatus(ReviewStatus.DRAFT);

        when(reviewRepository.findAll()).thenReturn(List.of(review));
        when(employeeServiceClient.getEmployee(11L)).thenReturn(new EmployeeDto(11L, "Reviewed Employee", 3L));
        when(employeeServiceClient.getEmployee(22L)).thenReturn(new EmployeeDto(22L, "Manager User", 3L));

        TeamReviewResponse response = performanceService.getTeamReviews(1L, "ADMIN");

        assertEquals(1, response.getReviews().size());
        assertEquals("Reviewed Employee", response.getReviews().get(0).getEmployeeName());
        assertEquals("Manager User", response.getReviews().get(0).getReviewerName());
    }
}
