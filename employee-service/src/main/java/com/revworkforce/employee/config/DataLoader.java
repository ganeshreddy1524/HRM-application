package com.revworkforce.employee.config;

import com.revworkforce.employee.entity.Employee;
import com.revworkforce.employee.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadDefaultEmployee(EmployeeRepository employeeRepository) {
        return args -> {
            if (!employeeRepository.existsByUserId(1L)) {
                Employee adminEmployee = Employee.builder()
                        .userId(1L)
                        .email("admin@revworkforce.com")
                        .fullName("System Administrator")
                        .departmentId(1L)
                        .designationId(4L)
                        .joiningDate(LocalDate.now())
                        .salary(1.0)
                        .status("ACTIVE")
                        .roleId(3)
                        .build();

                employeeRepository.save(adminEmployee);
            }
        };
    }
}
