package com.revworkforce.performance.controller;

import com.revworkforce.performance.dto.PerformanceReviewRequest;
import com.revworkforce.performance.dto.PerformanceReviewResponse;
import com.revworkforce.performance.dto.TeamReviewResponse;
import com.revworkforce.performance.service.PerformanceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private static final Logger log = LoggerFactory.getLogger(PerformanceController.class);

    private final PerformanceService performanceService;

    @Autowired
    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @PostMapping
    public ResponseEntity<PerformanceReviewResponse> createReview(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody PerformanceReviewRequest request) {
        log.info("Create performance review reviewerUserId={} role={} employeeId={}", userId, role, request.getEmployeeId());
        PerformanceReviewResponse response = performanceService.createReview(userId, request, role);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<PerformanceReviewResponse>> getMyReviews(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Get my reviews userId={}", userId);
        List<PerformanceReviewResponse> reviews = performanceService.getMyReviews(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/team")
    public ResponseEntity<TeamReviewResponse> getTeamReviews(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        log.info("Get team reviews userId={} role={}", userId, role);
        TeamReviewResponse response = performanceService.getTeamReviews(userId, role);
        return ResponseEntity.ok(response);
    }
}
