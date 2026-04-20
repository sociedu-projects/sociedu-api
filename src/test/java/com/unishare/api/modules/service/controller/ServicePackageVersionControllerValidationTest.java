package com.unishare.api.modules.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unishare.api.config.GlobalExceptionHandler;
import com.unishare.api.modules.service.dto.request.CreateServicePackageVersionRequest;
import com.unishare.api.modules.service.service.MentorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServicePackageVersionControllerValidationTest {

    private MockMvc mockMvc;
    private MentorService mentorService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mentorService = Mockito.mock(MentorService.class);
        objectMapper = new ObjectMapper();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ServicePackageController(mentorService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createPackageVersion_whenPriceNegative_shouldFailValidation() throws Exception {
        CreateServicePackageVersionRequest request = new CreateServicePackageVersionRequest();
        request.setPrice(new BigDecimal("-1"));
        request.setDuration(3);
        request.setDeliveryType("ONLINE");

        mockMvc.perform(post("/api/v1/service-packages/{id}/versions", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fields.price").value("Giá version phải lớn hơn hoặc bằng 0"));

        verifyNoInteractions(mentorService);
    }

    @Test
    void createPackageVersion_whenDurationLessThanOne_shouldFailValidation() throws Exception {
        CreateServicePackageVersionRequest request = new CreateServicePackageVersionRequest();
        request.setPrice(new BigDecimal("100"));
        request.setDuration(0);
        request.setDeliveryType("ONLINE");

        mockMvc.perform(post("/api/v1/service-packages/{id}/versions", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fields.duration").value("Thời lượng version phải lớn hơn hoặc bằng 1"));

        verifyNoInteractions(mentorService);
    }
}
