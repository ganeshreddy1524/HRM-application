package com.revworkforce.admin.service;

import com.revworkforce.admin.dto.DesignationRequest;
import com.revworkforce.admin.entity.Designation;
import com.revworkforce.admin.exception.DuplicateResourceException;
import com.revworkforce.admin.exception.ResourceNotFoundException;
import com.revworkforce.admin.repository.DesignationRepository;
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
class DesignationServiceTest {

    @Mock
    private DesignationRepository designationRepository;

    @InjectMocks
    private DesignationService service;

    @Test
    void getAllMapsEntitiesToResponses() {
        when(designationRepository.findAll()).thenReturn(List.of(
                new Designation(1L, "SE"),
                new Designation(2L, "SSE")
        ));

        var res = service.getAll();
        assertEquals(2, res.size());
        assertEquals("SE", res.get(0).getName());
        assertEquals(2L, res.get(1).getId());
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(designationRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getById(9L));
    }

    @Test
    void createThrowsOnDuplicateName() {
        DesignationRequest req = new DesignationRequest();
        req.setName("SE");
        when(designationRepository.existsByName("SE")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.create(req));
    }

    @Test
    void createSavesAndReturnsResponse() {
        DesignationRequest req = new DesignationRequest();
        req.setName("Architect");

        when(designationRepository.existsByName("Architect")).thenReturn(false);
        when(designationRepository.save(any(Designation.class))).thenReturn(new Designation(10L, "Architect"));

        var res = service.create(req);
        assertEquals(10L, res.getId());
        assertEquals("Architect", res.getName());
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(designationRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    void deleteDelegatesToRepository() {
        when(designationRepository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(designationRepository).deleteById(1L);
    }
}

