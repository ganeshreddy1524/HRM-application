package com.revworkforce.employee.controller;

import com.revworkforce.employee.dto.EmployeeSummaryResponse;
import com.revworkforce.employee.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internal/employees")
public class InternalController {

    private static final Logger log = LoggerFactory.getLogger(InternalController.class);

    private final EmployeeService employeeService;

    public InternalController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeSummaryResponse> getEmployeeById(@PathVariable Long id) {
        log.debug("Internal get employee summary id={}", id);
        EmployeeSummaryResponse employee = employeeService.getEmployeeSummary(id);
        return ResponseEntity.ok(employee);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<EmployeeSummaryResponse>> getEmployeesByIds(
            @RequestBody List<Long> ids) {
        log.debug("Internal batch employee summary size={}", ids != null ? ids.size() : 0);
        List<EmployeeSummaryResponse> employees = employeeService.getEmployeesByIds(ids);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<EmployeeSummaryResponse> getEmployeeByUserId(
            @PathVariable Long userId) {
        log.debug("Internal get employee summary by userId={}", userId);
        EmployeeSummaryResponse employee = employeeService.getEmployeeSummaryByUserId(userId);
        return ResponseEntity.ok(employee);
    }
}
