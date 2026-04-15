package com.unishare.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /** Spring Boot 4 / Jackson 3 có thể không đăng ký {@code com.fasterxml.jackson.databind.ObjectMapper}; VNPay cần bean này. */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
