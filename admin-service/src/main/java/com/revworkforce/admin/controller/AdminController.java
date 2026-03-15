package com.revworkforce.admin.controller;

import com.revworkforce.admin.dto.*;
import com.revworkforce.admin.exception.UnauthorizedException;
import com.revworkforce.admin.service.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final DepartmentService departmentService;
    private final DesignationService designationService;
    private final AnnouncementService announcementService;

    public AdminController(DepartmentService departmentService,
                          DesignationService designationService,
                          AnnouncementService announcementService) {
        this.departmentService = departmentService;
        this.designationService = designationService;
        this.announcementService = announcementService;
    }

    private void verifyAdminRole(String role) {
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("Access denied. Admin role required.");
        }
    }

    // Department endpoints
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments(@RequestHeader("X-User-Role") String role) {
        log.info("Get all departments role={}", role);
        verifyAdminRole(role);
        return ResponseEntity.ok(departmentService.getAll());
    }

    @PostMapping("/departments")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody DepartmentRequest request) {
        log.info("Create department role={} name={}", role, request.getName());
        verifyAdminRole(role);
        return new ResponseEntity<>(departmentService.create(request), HttpStatus.CREATED);
    }

    // Designation endpoints
    @GetMapping("/designations")
    public ResponseEntity<List<DesignationResponse>> getAllDesignations(@RequestHeader("X-User-Role") String role) {
        log.info("Get all designations role={}", role);
        verifyAdminRole(role);
        return ResponseEntity.ok(designationService.getAll());
    }

    @PostMapping("/designations")
    public ResponseEntity<DesignationResponse> createDesignation(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody DesignationRequest request) {
        log.info("Create designation role={} name={}", role, request.getName());
        verifyAdminRole(role);
        return new ResponseEntity<>(designationService.create(request), HttpStatus.CREATED);
    }

    // Announcement endpoints
    @GetMapping("/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getAllAnnouncements(@RequestHeader("X-User-Role") String role) {
        log.info("Get all announcements role={}", role);
        verifyAdminRole(role);
        return ResponseEntity.ok(announcementService.getAll());
    }

    @PostMapping("/announcements")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody AnnouncementRequest request) {
        log.info("Create announcement role={} title={}", role, request.getTitle());
        verifyAdminRole(role);
        return new ResponseEntity<>(announcementService.create(request), HttpStatus.CREATED);
    }
}
