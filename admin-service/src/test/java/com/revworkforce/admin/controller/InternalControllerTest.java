package com.revworkforce.admin.controller;

import com.revworkforce.admin.dto.DepartmentResponse;
import com.revworkforce.admin.dto.DesignationResponse;
import com.revworkforce.admin.service.DepartmentService;
import com.revworkforce.admin.service.DesignationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalControllerTest {

    @Mock
    private DepartmentService departmentService;
    @Mock
    private DesignationService designationService;

    @InjectMocks
    private InternalController controller;

    @Test
    void getDepartmentByIdReturnsOk() {
        when(departmentService.getById(1L)).thenReturn(DepartmentResponse.builder().id(1L).name("IT").build());
        var res = controller.getDepartmentById(1L);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("IT", res.getBody().getName());
    }
}
