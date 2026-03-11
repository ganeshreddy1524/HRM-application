package com.revworkforce.leave.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    // Security is handled at the API Gateway level.
    // Controllers read X-User-Id and X-User-Role directly from request headers.
}
