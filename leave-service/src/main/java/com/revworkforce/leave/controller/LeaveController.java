package com.revworkforce.leave.controller;

import com.revworkforce.leave.dto.*;
import com.revworkforce.leave.service.LeaveService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private static final Logger log = LoggerFactory.getLogger(LeaveController.class);

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    public ResponseEntity<LeaveResponse> applyLeave(
            @Valid @RequestBody LeaveApplyRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getUserIdFromRequest(httpRequest);
        log.info("Apply leave userId={} type={} start={} end={}", userId, request.getLeaveType(), request.getStartDate(), request.getEndDate());
        LeaveResponse response = leaveService.applyLeave(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<LeaveResponse>> getMyLeaves(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        log.info("Get my leaves userId={}", userId);
        List<LeaveResponse> leaves = leaveService.getMyLeaves(userId);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/summary")
    public ResponseEntity<LeaveSummaryResponse> getLeaveSummary(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        log.info("Get leave summary userId={}", userId);
        LeaveSummaryResponse summary = leaveService.getLeaveSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/team")
    public ResponseEntity<TeamLeaveResponse> getTeamLeaves(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        String role = getRoleFromRequest(httpRequest);
        log.info("Get team leaves userId={} role={}", userId, role);
        TeamLeaveResponse teamLeaves = leaveService.getTeamLeaves(userId, role);
        return ResponseEntity.ok(teamLeaves);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<LeaveResponse> cancelLeave(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        Long userId = getUserIdFromRequest(httpRequest);
        log.info("Cancel leave userId={} leaveId={}", userId, id);
        LeaveResponse response = leaveService.cancelLeave(userId, id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<LeaveResponse> approveLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveDecisionRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getUserIdFromRequest(httpRequest);
        String role = getRoleFromRequest(httpRequest);
        log.info("Approve leave userId={} role={} leaveId={}", userId, role, id);
        LeaveResponse response = leaveService.approveLeave(userId, id, request, role);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<LeaveResponse> rejectLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveDecisionRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getUserIdFromRequest(httpRequest);
        String role = getRoleFromRequest(httpRequest);
        log.info("Reject leave userId={} role={} leaveId={}", userId, role, id);
        LeaveResponse response = leaveService.rejectLeave(userId, id, request, role);
        return ResponseEntity.ok(response);
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr == null) {
            userIdStr = request.getHeader("X-User-Id");
        }
        if (userIdStr == null) {
            throw new RuntimeException("User ID not found in request");
        }
        return Long.parseLong(userIdStr);
    }

    private String getRoleFromRequest(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (role == null) {
            role = request.getHeader("X-User-Role");
        }
        return role != null ? role : "EMPLOYEE";
    }
}
