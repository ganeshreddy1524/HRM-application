package com.revworkforce.admin.service;

import com.revworkforce.admin.dto.HolidayRequest;
import com.revworkforce.admin.entity.Holiday;
import com.revworkforce.admin.exception.ResourceNotFoundException;
import com.revworkforce.admin.repository.HolidayRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HolidayServiceTest {

    @Mock
    private HolidayRepository holidayRepository;

    @InjectMocks
    private HolidayService service;

    @Test
    void getAllMapsEntitiesToResponses() {
        when(holidayRepository.findAll()).thenReturn(List.of(
                new Holiday(1L, "New Year", LocalDate.of(2026, 1, 1))
        ));
        var res = service.getAll();
        assertEquals(1, res.size());
        assertEquals("New Year", res.get(0).getName());
    }

    @Test
    void getByYearDelegatesToRepository() {
        when(holidayRepository.findByYear(2026)).thenReturn(List.of(
                new Holiday(1L, "New Year", LocalDate.of(2026, 1, 1))
        ));
        var res = service.getByYear(2026);
        assertEquals(1, res.size());
        assertEquals(LocalDate.of(2026, 1, 1), res.get(0).getDate());
    }

    @Test
    void createSavesAndReturnsResponse() {
        HolidayRequest req = new HolidayRequest();
        req.setName("Festival");
        req.setDate(LocalDate.of(2026, 3, 1));

        when(holidayRepository.save(any(Holiday.class))).thenReturn(new Holiday(10L, "Festival", LocalDate.of(2026, 3, 1)));

        var res = service.create(req);
        assertEquals(10L, res.getId());
        assertEquals("Festival", res.getName());
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(holidayRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    void deleteDelegatesToRepository() {
        when(holidayRepository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(holidayRepository).deleteById(1L);
    }
}

