package com.revworkforce.performance.service;

import com.revworkforce.performance.client.EmployeeServiceClient;
import com.revworkforce.performance.dto.EmployeeDto;
import com.revworkforce.performance.dto.PerformanceReviewRequest;
import com.revworkforce.performance.dto.ReviewFeedbackRequest;
import com.revworkforce.performance.dto.PerformanceReviewResponse;
import com.revworkforce.performance.dto.TeamReviewResponse;
import com.revworkforce.performance.entity.PerformanceReview;
import com.revworkforce.performance.enums.ReviewStatus;
import com.revworkforce.performance.exception.ResourceNotFoundException;
import com.revworkforce.performance.exception.UnauthorizedException;
import com.revworkforce.performance.repository.PerformanceReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

    @Test
    void createReviewRejectsEmployeeRole() {
        PerformanceReviewRequest request = new PerformanceReviewRequest();
        request.setEmployeeId(11L);
        assertThrows(UnauthorizedException.class, () -> performanceService.createReview(1L, request, "EMPLOYEE"));
    }

    @Test
    void createReviewThrowsWhenEmployeeMissing() {
        PerformanceReviewRequest request = new PerformanceReviewRequest();
        request.setEmployeeId(11L);
        when(employeeServiceClient.getEmployee(11L)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> performanceService.createReview(1L, request, "ADMIN"));
    }

    @Test
    void createReviewSavesAndNotifies() {
        PerformanceReviewRequest request = new PerformanceReviewRequest();
        request.setEmployeeId(11L);
        request.setKeyDeliverables("KD");
        request.setAccomplishments("A");
        request.setAreasOfImprovement("I");
        request.setSelfRating(4);

        when(employeeServiceClient.getEmployee(11L)).thenReturn(new EmployeeDto(11L, "Emp", 2L));
        when(reviewRepository.save(any(PerformanceReview.class))).thenAnswer(inv -> {
            PerformanceReview r = inv.getArgument(0);
            r.setId(50L);
            return r;
        });
        when(employeeServiceClient.getEmployee(11L)).thenReturn(new EmployeeDto(11L, "Emp", 2L));
        when(employeeServiceClient.getEmployee(1L)).thenReturn(new EmployeeDto(1L, "Mgr", 3L));

        PerformanceReviewResponse res = performanceService.createReview(1L, request, "MANAGER");
        assertEquals(50L, res.getId());
        verify(notificationService).notifyReviewCreated(any(PerformanceReview.class));
    }

    @Test
    void getMyReviewsThrowsWhenEmployeeMissing() {
        when(employeeServiceClient.getEmployeeByUserId(1L)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> performanceService.getMyReviews(1L));
    }

    @Test
    void getTeamReviewsForManagerUsesReviewerId() {
        PerformanceReview review = new PerformanceReview();
        review.setId(100L);
        review.setEmployeeId(11L);
        review.setReviewerId(22L);
        review.setStatus(ReviewStatus.DRAFT);

        when(employeeServiceClient.getEmployeeByUserId(1L)).thenReturn(new EmployeeDto(22L, "Manager Employee", 3L));
        when(reviewRepository.findByReviewerId(22L)).thenReturn(List.of(review));
        when(employeeServiceClient.getEmployee(11L)).thenReturn(new EmployeeDto(11L, "Reviewed Employee", 3L));
        when(employeeServiceClient.getEmployee(22L)).thenReturn(new EmployeeDto(22L, "Manager User", 3L));

        TeamReviewResponse response = performanceService.getTeamReviews(1L, "MANAGER");
        assertEquals(1, response.getReviews().size());
    }

    @Test
    void submitReviewRejectsWhenNotOwner() {
        PerformanceReview review = new PerformanceReview();
        review.setId(1L);
        review.setEmployeeId(11L);
        review.setStatus(ReviewStatus.DRAFT);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(employeeServiceClient.getEmployeeByUserId(1L)).thenReturn(new EmployeeDto(99L, "Other", 2L));

        assertThrows(UnauthorizedException.class, () -> performanceService.submitReview(1L, 1L));
    }

    @Test
    void submitReviewRejectsWhenNotDraft() {
        PerformanceReview review = new PerformanceReview();
        review.setId(1L);
        review.setEmployeeId(11L);
        review.setStatus(ReviewStatus.SUBMITTED);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(employeeServiceClient.getEmployeeByUserId(1L)).thenReturn(new EmployeeDto(11L, "Owner", 2L));

        assertThrows(IllegalStateException.class, () -> performanceService.submitReview(1L, 1L));
    }

    @Test
    void provideFeedbackUpdatesWhenSubmitted() {
        PerformanceReview review = new PerformanceReview();
        review.setId(1L);
        review.setEmployeeId(11L);
        review.setReviewerId(22L);
        review.setStatus(ReviewStatus.SUBMITTED);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(PerformanceReview.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeServiceClient.getEmployee(11L)).thenReturn(new EmployeeDto(11L, "Emp", 2L));
        when(employeeServiceClient.getEmployee(22L)).thenReturn(new EmployeeDto(22L, "Mgr", 3L));

        ReviewFeedbackRequest req = new ReviewFeedbackRequest();
        req.setManagerFeedback("Good");
        req.setManagerRating(5);

        PerformanceReviewResponse response = performanceService.provideFeedback(2L, 1L, req, "ADMIN");
        assertEquals(ReviewStatus.REVIEWED, response.getStatus());
        verify(notificationService).notifyFeedbackProvided(any(PerformanceReview.class));
    }
}
