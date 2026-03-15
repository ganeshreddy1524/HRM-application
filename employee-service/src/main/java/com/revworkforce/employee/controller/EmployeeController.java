package com.revworkforce.employee.controller;

import com.revworkforce.employee.dto.*;
import com.revworkforce.employee.exception.UnauthorizedException;
import com.revworkforce.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/profile")
    public ResponseEntity<EmployeeProfileResponse> getProfile(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Get profile userId={}", userId);
        EmployeeProfileResponse profile = employeeService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<EmployeeProfileResponse> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        log.info("Update profile userId={}", userId);
        EmployeeProfileResponse profile = employeeService.updateProfile(userId, request);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/team")
    public ResponseEntity<List<TeamMemberResponse>> getTeam(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Get team userId={}", userId);
        List<TeamMemberResponse> team = employeeService.getTeam(userId);
        return ResponseEntity.ok(team);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
            @RequestHeader("X-User-Role") String role) {
        log.info("Get all employees role={}", role);
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("Only admins can view all employees");
        }
        List<EmployeeResponse> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody EmployeeCreateRequest request) {
        log.info("Create employee request role={} userId={}", role, request.getUserId());
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("Only admins can create employees");
        }
        EmployeeResponse employee = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        log.info("Get employee by id id={}", id);
        EmployeeResponse employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }
}
