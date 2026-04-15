package com.unishare.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * URL gốc FE/BE — cấu hình tập trung trong {@code application.yaml} ({@code app.urls.*}).
 */
@ConfigurationProperties(prefix = "app.urls")
public record AppUrlsProperties(
        String frontendBase,
        String apiPublicBase,
        List<String> corsAllowedOrigins
) {}
