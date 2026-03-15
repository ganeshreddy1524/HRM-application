package com.revworkforce.employee.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    // Internal service-to-service call (auth-service exposes this without JWT).
    @GetMapping("/api/internal/users/{userId}")
    Map<String, Object> getUserById(@PathVariable("userId") Long userId);
}
