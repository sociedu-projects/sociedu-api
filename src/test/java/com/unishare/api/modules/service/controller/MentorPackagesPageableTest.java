package com.unishare.api.modules.service.controller;

import com.unishare.api.modules.service.dto.MentorDto;
import com.unishare.api.modules.service.service.MentorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MentorPackagesPageableTest {

    private MockMvc mockMvc;
    private MentorService mentorService;

    @BeforeEach
    void setUp() {
        mentorService = Mockito.mock(MentorService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new MentorController(mentorService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getMentorPackages_whenPageParamsProvided_shouldReturnPagedData() throws Exception {
        UUID mentorId = UUID.randomUUID();
        MentorDto.ServicePackageResponse response = MentorDto.ServicePackageResponse.builder()
                .id(UUID.randomUUID())
                .mentorId(mentorId)
                .name("Career Planning")
                .isActive(true)
                .build();

        when(mentorService.getMentorPackages(eq(mentorId), eq(PageRequest.of(1, 5))))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(1, 5), 1));

        mockMvc.perform(get("/api/v1/mentors/{id}/packages", mentorId)
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Career Planning"))
                .andExpect(jsonPath("$.data.number").value(1))
                .andExpect(jsonPath("$.data.size").value(5));
    }
}
