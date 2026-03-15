package com.revworkforce.employee.service;

import com.revworkforce.employee.client.AdminServiceClient;
import com.revworkforce.employee.client.AuthServiceClient;
import com.revworkforce.employee.entity.Employee;
import com.revworkforce.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectoryServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AdminServiceClient adminServiceClient;
    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private DirectoryService service;

    @Test
    void searchEmployeesMapsDepartmentAndDesignationAndEmployeeCode() {
        Employee e = Employee.builder()
                .id(1L)
                .userId(10L)
                .email("a@b.com")
                .fullName("Name")
                .departmentId(1L)
                .designationId(2L)
                .build();

        when(employeeRepository.searchByNameOrEmail("q")).thenReturn(List.of(e));
        when(adminServiceClient.getDepartmentById(1L)).thenReturn(Map.of("name", "IT"));
        when(adminServiceClient.getDesignationById(2L)).thenReturn(Map.of("name", "SE"));
        when(authServiceClient.getUserById(10L)).thenReturn(Map.of("employeeId", "E10"));

        var res = service.searchEmployees("q");
        assertEquals(1, res.size());
        assertEquals("IT", res.get(0).getDepartmentName());
        assertEquals("SE", res.get(0).getDesignationName());
        assertEquals("E10", res.get(0).getEmployeeId());
    }

    @Test
    void lookupFallbacksReturnUnknownOrUserId() {
        Employee e = Employee.builder()
                .id(1L)
                .userId(10L)
                .email("a@b.com")
                .fullName("Name")
                .departmentId(1L)
                .designationId(2L)
                .build();

        when(employeeRepository.findByDepartmentId(1L)).thenReturn(List.of(e));
        when(adminServiceClient.getDepartmentById(1L)).thenThrow(new RuntimeException("down"));
        when(adminServiceClient.getDesignationById(2L)).thenThrow(new RuntimeException("down"));
        when(authServiceClient.getUserById(10L)).thenThrow(new RuntimeException("down"));

        var res = service.getEmployeesByDepartment(1L);
        assertEquals("Unknown Department", res.get(0).getDepartmentName());
        assertEquals("Unknown Designation", res.get(0).getDesignationName());
        assertEquals("10", res.get(0).getEmployeeId());
    }
}

