package com.revworkforce.admin.service;

import com.revworkforce.admin.dto.DepartmentRequest;
import com.revworkforce.admin.entity.Department;
import com.revworkforce.admin.exception.DuplicateResourceException;
import com.revworkforce.admin.exception.ResourceNotFoundException;
import com.revworkforce.admin.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService service;

    @Test
    void getAllMapsEntitiesToResponses() {
        when(departmentRepository.findAll()).thenReturn(List.of(
                new Department(1L, "IT"),
                new Department(2L, "HR")
        ));

        var res = service.getAll();
        assertEquals(2, res.size());
        assertEquals("IT", res.get(0).getName());
        assertEquals(2L, res.get(1).getId());
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(departmentRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getById(9L));
    }

    @Test
    void createThrowsOnDuplicateName() {
        DepartmentRequest req = new DepartmentRequest();
        req.setName("IT");
        when(departmentRepository.existsByName("IT")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.create(req));
    }

    @Test
    void createSavesAndReturnsResponse() {
        DepartmentRequest req = new DepartmentRequest();
        req.setName("Finance");

        when(departmentRepository.existsByName("Finance")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(new Department(10L, "Finance"));

        var res = service.create(req);
        assertEquals(10L, res.getId());
        assertEquals("Finance", res.getName());
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(departmentRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    void deleteDelegatesToRepository() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(departmentRepository).deleteById(1L);
    }
}

