package com.revworkforce.employee.controller;

import com.revworkforce.employee.dto.EmployeeSearchResponse;
import com.revworkforce.employee.service.DirectoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectoryControllerTest {

    @Mock
    private DirectoryService directoryService;

    @InjectMocks
    private DirectoryController controller;

    @Test
    void searchEmployeesReturnsOk() {
        when(directoryService.searchEmployees("q")).thenReturn(List.of(EmployeeSearchResponse.builder().id(1L).build()));
        var res = controller.searchEmployees("q");
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
    }

    @Test
    void getEmployeesByDepartmentReturnsOk() {
        when(directoryService.getEmployeesByDepartment(1L)).thenReturn(List.of());
        var res = controller.getEmployeesByDepartment(1L);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(0, res.getBody().size());
    }
}

