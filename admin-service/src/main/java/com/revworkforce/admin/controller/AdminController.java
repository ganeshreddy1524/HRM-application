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
    private final HolidayService holidayService;
    private final AnnouncementService announcementService;

    public AdminController(DepartmentService departmentService,
                          DesignationService designationService,
                          HolidayService holidayService,
                          AnnouncementService announcementService) {
        this.departmentService = departmentService;
        this.designationService = designationService;
        this.holidayService = holidayService;
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

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Void> deleteDepartment(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        log.info("Delete department role={} id={}", role, id);
        verifyAdminRole(role);
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
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

    @DeleteMapping("/designations/{id}")
    public ResponseEntity<Void> deleteDesignation(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        log.info("Delete designation role={} id={}", role, id);
        verifyAdminRole(role);
        designationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Holiday endpoints
    @GetMapping("/holidays")
    public ResponseEntity<List<HolidayResponse>> getAllHolidays(@RequestHeader("X-User-Role") String role) {
        log.info("Get all holidays role={}", role);
        verifyAdminRole(role);
        return ResponseEntity.ok(holidayService.getAll());
    }

    @GetMapping("/holidays/year/{year}")
    public ResponseEntity<List<HolidayResponse>> getHolidaysByYear(
            @RequestHeader("X-User-Role") String role,
            @PathVariable int year) {
        log.info("Get holidays by year role={} year={}", role, year);
        verifyAdminRole(role);
        return ResponseEntity.ok(holidayService.getByYear(year));
    }

    @PostMapping("/holidays")
    public ResponseEntity<HolidayResponse> createHoliday(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody HolidayRequest request) {
        log.info("Create holiday role={} date={}", role, request.getDate());
        verifyAdminRole(role);
        return new ResponseEntity<>(holidayService.create(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/holidays/{id}")
    public ResponseEntity<Void> deleteHoliday(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        log.info("Delete holiday role={} id={}", role, id);
        verifyAdminRole(role);
        holidayService.delete(id);
        return ResponseEntity.noContent().build();
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

    @PutMapping("/announcements/{id}")
    public ResponseEntity<AnnouncementResponse> updateAnnouncement(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementRequest request) {
        log.info("Update announcement role={} id={}", role, id);
        verifyAdminRole(role);
        return ResponseEntity.ok(announcementService.update(id, request));
    }

    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<Void> deleteAnnouncement(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        log.info("Delete announcement role={} id={}", role, id);
        verifyAdminRole(role);
        announcementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
