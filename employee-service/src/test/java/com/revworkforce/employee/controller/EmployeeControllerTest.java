package com.revworkforce.employee.controller;

import com.revworkforce.employee.dto.*;
import com.revworkforce.employee.exception.UnauthorizedException;
import com.revworkforce.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController controller;

    @Test
    void getProfileReturnsOk() {
        when(employeeService.getProfile(10L)).thenReturn(EmployeeProfileResponse.builder().userId(10L).fullName("X").build());
        var res = controller.getProfile(10L);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("X", res.getBody().getFullName());
    }

    @Test
    void getAllEmployeesRequiresAdmin() {
        assertThrows(UnauthorizedException.class, () -> controller.getAllEmployees("EMPLOYEE"));
    }

    @Test
    void getAllEmployeesReturnsOkForAdmin() {
        when(employeeService.getAllEmployees()).thenReturn(List.of(EmployeeResponse.builder().id(1L).build()));
        var res = controller.getAllEmployees("ADMIN");
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
    }

    @Test
    void createEmployeeRequiresAdmin() {
        assertThrows(UnauthorizedException.class, () -> controller.createEmployee("EMPLOYEE", new EmployeeCreateRequest()));
    }

    @Test
    void createEmployeeReturnsCreated() {
        EmployeeCreateRequest req = new EmployeeCreateRequest();
        req.setUserId(10L);
        when(employeeService.createEmployee(req)).thenReturn(EmployeeResponse.builder().id(2L).userId(10L).build());
        var res = controller.createEmployee("ADMIN", req);
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertEquals(2L, res.getBody().getId());
    }

    @Test
    void logoutLikeEndpointsCallService() {
        when(employeeService.getEmployeeById(1L)).thenReturn(EmployeeResponse.builder().id(1L).build());
        var res = controller.getEmployeeById(1L);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        verify(employeeService).getEmployeeById(1L);
    }
}
