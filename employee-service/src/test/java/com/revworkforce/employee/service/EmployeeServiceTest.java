package com.revworkforce.employee.service;

import com.revworkforce.employee.client.AdminServiceClient;
import com.revworkforce.employee.client.AuthServiceClient;
import com.revworkforce.employee.dto.EmployeeProfileResponse;
import com.revworkforce.employee.dto.EmployeeUpdateRequest;
import com.revworkforce.employee.entity.Employee;
import com.revworkforce.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AdminServiceClient adminServiceClient;
    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void updateProfileCreatesMissingEmployeeProfileFromAuthService() {
        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
        request.setPhone("+911234567890");
        request.setAddress("Hyderabad");
        request.setEmergencyContact("9999999999");

        when(employeeRepository.findByUserId(22L)).thenReturn(Optional.empty());
        when(authServiceClient.getUserById(22L)).thenReturn(Map.of(
                "email", "user@revworkforce.com",
                "fullName", "User Twenty Two",
                "roleId", 1
        ));
        when(adminServiceClient.getDepartmentById(1L)).thenReturn(Map.of("name", "IT"));
        when(adminServiceClient.getDesignationById(1L)).thenReturn(Map.of("name", "Software Engineer"));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeProfileResponse response = employeeService.updateProfile(22L, request);

        assertEquals("User Twenty Two", response.getFullName());
        assertEquals("user@revworkforce.com", response.getEmail());
        assertEquals("+911234567890", response.getPhone());
        assertEquals("Hyderabad", response.getAddress());
        assertEquals("IT", response.getDepartmentName());
        assertEquals("Software Engineer", response.getDesignationName());
    }
}
