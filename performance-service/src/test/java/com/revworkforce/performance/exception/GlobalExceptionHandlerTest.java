package com.revworkforce.performance.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesResourceNotFound() {
        var response = handler.handleResourceNotFoundException(new ResourceNotFoundException("missing"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
    }

    @Test
    void handlesUnauthorized() {
        var response = handler.handleUnauthorizedException(new UnauthorizedException("nope"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().getStatus());
    }

    @Test
    void handlesGenericException() {
        var response = handler.handleGenericException(new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
    }
}

