package com.revworkforce.auth.controller;

import com.revworkforce.auth.dto.AdminCreateUserRequest;
import com.revworkforce.auth.entity.User;
import com.revworkforce.auth.exception.UnauthorizedException;
import com.revworkforce.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserController controller;

    @Test
    void getAllUsersRequiresAdminRole() {
        assertThrows(UnauthorizedException.class, () -> controller.getAllUsers("EMPLOYEE"));
    }

    @Test
    void getAllUsersReturnsOkForAdmin() {
        when(authService.getAllUsers()).thenReturn(List.of(
                User.builder().id(1L).employeeId("1001").fullName("U").email("u@x.com").roleId(1).status("ACTIVE").build()
        ));
        var response = controller.getAllUsers("ADMIN");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void createUserRequiresAdminRole() {
        assertThrows(UnauthorizedException.class, () -> controller.createUser("EMPLOYEE", new AdminCreateUserRequest()));
    }

    @Test
    void createUserReturnsCreated() {
        AdminCreateUserRequest req = new AdminCreateUserRequest();
        req.setEmployeeId("100");
        when(authService.createUser(req)).thenReturn(
                User.builder().id(10L).employeeId("100").fullName("U").email("u@x.com").roleId(1).status("ACTIVE").build()
        );
        var response = controller.createUser("ADMIN", req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(10L, response.getBody().getId());
    }

    @Test
    void searchUsersReturnsOk() {
        when(authService.searchUsers("q")).thenReturn(List.of(
                User.builder().id(1L).employeeId("1001").fullName("U").email("u@x.com").roleId(1).status("ACTIVE").build()
        ));
        var response = controller.searchUsers("q");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void setUserActiveRequiresAdminRole() {
        assertThrows(UnauthorizedException.class, () -> controller.setUserActive("EMPLOYEE", 1L, true));
    }

    @Test
    void getCurrentUserReturnsOk() {
        when(authService.getUserById(1L)).thenReturn(
                User.builder().id(1L).employeeId("1001").fullName("U").email("u@x.com").roleId(1).status("ACTIVE").build()
        );
        var response = controller.getCurrentUser(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }
}
