package com.unishare.api.modules.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unishare.api.config.GlobalExceptionHandler;
import com.unishare.api.modules.service.dto.MentorDto;
import com.unishare.api.modules.service.service.MentorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MentorProfileUpdateValidationTest {

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
    void updateMyProfile_whenHeadlineBlank_shouldFailValidation() throws Exception {
        MentorDto.MentorProfileRequest request = validRequest();
        request.setHeadline(" ");

        mockMvc.perform(put("/api/v1/mentors/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fields.headline").value("Headline khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng"));

        verifyNoInteractions(mentorService);
    }

    @Test
    void updateMyProfile_whenExpertiseBlank_shouldFailValidation() throws Exception {
        MentorDto.MentorProfileRequest request = validRequest();
        request.setExpertise(" ");

        mockMvc.perform(put("/api/v1/mentors/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fields.expertise").value("Expertise khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng"));

        verifyNoInteractions(mentorService);
    }

    @Test
    void updateMyProfile_whenBasePriceNegative_shouldFailValidation() throws Exception {
        MentorDto.MentorProfileRequest request = validRequest();
        request.setBasePrice(new java.math.BigDecimal("-1"));

        mockMvc.perform(put("/api/v1/mentors/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fields.basePrice").value("Base price pháº£i lá»›n hÆ¡n hoáº·c báº±ng 0"));

        verifyNoInteractions(mentorService);
    }

    private MentorDto.MentorProfileRequest validRequest() {
        MentorDto.MentorProfileRequest request = new MentorDto.MentorProfileRequest();
        request.setHeadline("Career mentor");
        request.setExpertise("Product");
        request.setBasePrice(new java.math.BigDecimal("100.00"));
        return request;
    }
}
