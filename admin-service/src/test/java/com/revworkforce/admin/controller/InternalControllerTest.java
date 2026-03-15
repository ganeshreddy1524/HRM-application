package com.revworkforce.admin.controller;

import com.revworkforce.admin.dto.DepartmentResponse;
import com.revworkforce.admin.dto.DesignationResponse;
import com.revworkforce.admin.dto.HolidayResponse;
import com.revworkforce.admin.service.DepartmentService;
import com.revworkforce.admin.service.DesignationService;
import com.revworkforce.admin.service.HolidayService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalControllerTest {

    @Mock
    private DepartmentService departmentService;
    @Mock
    private DesignationService designationService;
    @Mock
    private HolidayService holidayService;

    @InjectMocks
    private InternalController controller;

    @Test
    void getDepartmentByIdReturnsOk() {
        when(departmentService.getById(1L)).thenReturn(DepartmentResponse.builder().id(1L).name("IT").build());
        var res = controller.getDepartmentById(1L);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("IT", res.getBody().getName());
    }

    @Test
    void getHolidaysByYearReturnsOk() {
        when(holidayService.getByYear(2026)).thenReturn(List.of(
                HolidayResponse.builder().id(1L).name("New Year").date(LocalDate.of(2026, 1, 1)).build()
        ));
        var res = controller.getHolidaysByYear(2026);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
    }
}

