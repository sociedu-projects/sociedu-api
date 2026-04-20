package com.unishare.api.modules.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unishare.api.config.GlobalExceptionHandler;
import com.unishare.api.modules.service.dto.request.CreateServicePackageRequest;
import com.unishare.api.modules.service.service.MentorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MentorControllerValidationTest {

    private MockMvc mockMvc;
    private MentorService mentorService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mentorService = Mockito.mock(MentorService.class);
        objectMapper = new ObjectMapper();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new MentorController(mentorService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void addPackage_whenCurriculumsEmpty_shouldFailValidation() throws Exception {
        CreateServicePackageRequest request = validRequest();
        request.setCurriculums(List.of());

        mockMvc.perform(post("/api/v1/mentors/me/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fields.curriculums").value("Danh sách curriculum không được để trống"));

        verifyNoInteractions(mentorService);
    }

    @Test
    void addPackage_whenPriceNegative_shouldFailValidation() throws Exception {
        CreateServicePackageRequest request = validRequest();
        request.setPrice(new BigDecimal("-1.00"));

        mockMvc.perform(post("/api/v1/mentors/me/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fields.price").value("Giá gói dịch vụ phải lớn hơn hoặc bằng 0"));

        verifyNoInteractions(mentorService);
    }

    @Test
    void addPackage_whenDurationLessThanOne_shouldFailValidation() throws Exception {
        CreateServicePackageRequest request = validRequest();
        request.setDuration(0);

        mockMvc.perform(post("/api/v1/mentors/me/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fields.duration").value("Thời lượng gói dịch vụ phải lớn hơn hoặc bằng 1"));

        verifyNoInteractions(mentorService);
    }

    private CreateServicePackageRequest validRequest() {
        CreateServicePackageRequest request = new CreateServicePackageRequest();
        request.setName("Career Planning");
        request.setDescription("Package description");
        request.setPrice(new BigDecimal("120.00"));
        request.setDuration(3);
        request.setDeliveryType("ONLINE");

        CreateServicePackageRequest.CurriculumRequest curriculum = new CreateServicePackageRequest.CurriculumRequest();
        curriculum.setTitle("Session 1");
        curriculum.setDescription("Intro");
        curriculum.setOrderIndex(1);
        curriculum.setDuration(60);
        request.setCurriculums(List.of(curriculum));
        return request;
    }
}
