package com.unishare.api.modules.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unishare.api.config.GlobalExceptionHandler;
import com.unishare.api.modules.service.dto.request.ReviewReportRequest;
import com.unishare.api.modules.service.entity.ReportStatus;
import com.unishare.api.modules.service.service.ProgressReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MentorReportControllerValidationTest {

    private MockMvc mockMvc;
    private ProgressReportService progressReportService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        progressReportService = Mockito.mock(ProgressReportService.class);
        objectMapper = new ObjectMapper();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new MentorReportController(progressReportService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void reviewReport_whenMentorFeedbackBlank_shouldFailValidation() throws Exception {
        ReviewReportRequest request = new ReviewReportRequest();
        request.setStatus(ReportStatus.REVIEWED);
        request.setMentorFeedback(" ");

        mockMvc.perform(put("/api/v1/mentors/me/reports/{id}/feedback", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fields.mentorFeedback").value("Nhận xét không được để trống"));

        verifyNoInteractions(progressReportService);
    }

    @Test
    void reviewReport_whenStatusMissing_shouldFailValidation() throws Exception {
        ReviewReportRequest request = new ReviewReportRequest();
        request.setMentorFeedback("Reviewed");

        mockMvc.perform(put("/api/v1/mentors/me/reports/{id}/feedback", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fields.status").value("Trạng thái không được để trống"));

        verifyNoInteractions(progressReportService);
    }
}
