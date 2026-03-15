package com.revworkforce.employee.controller;

import com.revworkforce.employee.dto.EmployeeSummaryResponse;
import com.revworkforce.employee.service.EmployeeService;
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
class InternalControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private InternalController controller;

    @Test
    void getEmployeeByIdReturnsOk() {
        when(employeeService.getEmployeeSummary(1L)).thenReturn(EmployeeSummaryResponse.builder().id(1L).build());
        var res = controller.getEmployeeById(1L);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1L, res.getBody().getId());
    }

    @Test
    void batchReturnsOk() {
        when(employeeService.getEmployeesByIds(List.of(1L, 2L))).thenReturn(List.of(EmployeeSummaryResponse.builder().id(1L).build()));
        var res = controller.getEmployeesByIds(List.of(1L, 2L));
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
    }
}

