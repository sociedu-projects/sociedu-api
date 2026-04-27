package com.unishare.api.modules.service.controller;

import com.unishare.api.modules.mentor.controller.MentorController;
import com.unishare.api.modules.mentor.dto.MentorResponse;
import com.unishare.api.modules.mentor.service.MentorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MentorProfileControllerTest {

    private MockMvc mockMvc;
    private MentorService mentorProfileService;

    @BeforeEach
    void setUp() {
        mentorProfileService = Mockito.mock(MentorService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new MentorController(mentorProfileService)).build();
    }

    @Test
    void getMentorProfile_shouldReturnProfileWithoutPackages() throws Exception {
        UUID mentorId = UUID.randomUUID();
        MentorResponse response = MentorResponse.builder()
                .userId(mentorId)
                .headline("Career mentor")
                .verificationStatus("verified")
                .build();

        when(mentorProfileService.getMentorProfile(eq(mentorId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/mentors/{id}", mentorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(mentorId.toString()))
                .andExpect(jsonPath("$.data.headline").value("Career mentor"))
                .andExpect(jsonPath("$.data.packages").doesNotExist());
    }
}
