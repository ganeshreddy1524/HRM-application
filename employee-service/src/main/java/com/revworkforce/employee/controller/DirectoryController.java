package com.revworkforce.employee.controller;

import com.revworkforce.employee.dto.EmployeeSearchResponse;
import com.revworkforce.employee.service.DirectoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class DirectoryController {

    private static final Logger log = LoggerFactory.getLogger(DirectoryController.class);

    private final DirectoryService directoryService;

    public DirectoryController(DirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeSearchResponse>> searchEmployees(
            @RequestParam("q") String query) {
        log.info("Search employees q={}", query);
        List<EmployeeSearchResponse> employees = directoryService.searchEmployees(query);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<EmployeeSearchResponse>> getEmployeesByDepartment(
            @PathVariable Long departmentId) {
        log.info("Get employees by department departmentId={}", departmentId);
        List<EmployeeSearchResponse> employees = directoryService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(employees);
    }
}
