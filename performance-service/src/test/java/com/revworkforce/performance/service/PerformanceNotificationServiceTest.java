package com.revworkforce.performance.service;

import com.revworkforce.performance.entity.Goal;
import com.revworkforce.performance.entity.PerformanceNotification;
import com.revworkforce.performance.entity.PerformanceReview;
import com.revworkforce.performance.enums.ReviewStatus;
import com.revworkforce.performance.exception.ResourceNotFoundException;
import com.revworkforce.performance.repository.PerformanceNotificationRepository;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerformanceNotificationServiceTest {

    @Mock
    private PerformanceNotificationRepository notificationRepository;

    @InjectMocks
    private PerformanceNotificationService service;

    @Test
    void createNotificationSavesEntity() {
        when(notificationRepository.save(any(PerformanceNotification.class))).thenAnswer(inv -> inv.getArgument(0));
        service.createNotification(10L, "hello");
        verify(notificationRepository).save(any(PerformanceNotification.class));
    }

    @Test
    void getNotificationsDelegatesToRepository() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(new PerformanceNotification()));
        assertEquals(1, service.getNotifications(10L).size());
    }

    @Test
    void markAsReadThrowsWhenMissing() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.markAsRead(1L));
    }

    @Test
    void markAsReadUpdatesFlag() {
        PerformanceNotification n = new PerformanceNotification();
        n.setId(1L);
        n.setReadFlag(false);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(PerformanceNotification.class))).thenAnswer(inv -> inv.getArgument(0));

        service.markAsRead(1L);

        verify(notificationRepository).save(any(PerformanceNotification.class));
    }

    @Test
    void notifyHelpersCreateNotifications() {
        when(notificationRepository.save(any(PerformanceNotification.class))).thenAnswer(inv -> inv.getArgument(0));

        PerformanceReview review = new PerformanceReview();
        review.setId(1L);
        review.setEmployeeId(11L);
        review.setReviewerId(22L);
        review.setStatus(ReviewStatus.DRAFT);

        service.notifyReviewCreated(review);
        service.notifyFeedbackProvided(review);

        Goal goal = new Goal();
        goal.setId(2L);
        goal.setEmployeeId(11L);
        service.notifyGoalCommented(goal);

        verify(notificationRepository, times(3)).save(any(PerformanceNotification.class));
    }
}
