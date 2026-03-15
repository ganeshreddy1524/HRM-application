package com.revworkforce.admin.controller;

import com.revworkforce.admin.dto.*;
import com.revworkforce.admin.exception.UnauthorizedException;
import com.revworkforce.admin.service.AnnouncementService;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private DepartmentService departmentService;
    @Mock
    private DesignationService designationService;
    @Mock
    private HolidayService holidayService;
    @Mock
    private AnnouncementService announcementService;

    @InjectMocks
    private AdminController controller;

    @Test
    void endpointsRejectNonAdminRole() {
        assertThrows(UnauthorizedException.class, () -> controller.getAllDepartments("EMPLOYEE"));
        assertThrows(UnauthorizedException.class, () -> controller.getAllDesignations("MANAGER"));
        assertThrows(UnauthorizedException.class, () -> controller.getAllHolidays("EMPLOYEE"));
        assertThrows(UnauthorizedException.class, () -> controller.getAllAnnouncements("EMPLOYEE"));
    }

    @Test
    void getAllDepartmentsReturnsOk() {
        when(departmentService.getAll()).thenReturn(List.of(DepartmentResponse.builder().id(1L).name("IT").build()));
        var res = controller.getAllDepartments("ADMIN");
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
    }

    @Test
    void createHolidayReturnsCreated() {
        HolidayRequest req = new HolidayRequest();
        req.setName("Festival");
        req.setDate(LocalDate.of(2026, 3, 1));
        when(holidayService.create(req)).thenReturn(HolidayResponse.builder().id(2L).name("Festival").date(req.getDate()).build());

        var res = controller.createHoliday("ADMIN", req);
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertEquals(2L, res.getBody().getId());
    }
}

