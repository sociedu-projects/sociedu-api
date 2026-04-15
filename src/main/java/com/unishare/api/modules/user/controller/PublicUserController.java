package com.unishare.api.modules.user.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.modules.user.dto.*;
import com.unishare.api.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PermitAll;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users — Public profile")
public class PublicUserController {

    private final UserService userService;

    @Operation(summary = "Hồ sơ công khai theo user id")
    @PermitAll
    @SecurityRequirements(value = {})
    @GetMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserFullProfileResponse>> getFullProfile(@PathVariable Long id) {
        UserProfileResponse profile = userService.getProfile(id);
        
        UserFullProfileResponse fullProfile = UserFullProfileResponse.builder()
                .profile(profile)
                .educations(userService.getEducations(id))
                .languages(userService.getLanguages(id))
                .experiences(userService.getExperiences(id))
                .certificates(userService.getCertificates(id))
                .build();

        return ResponseEntity.ok(ApiResponse.<UserFullProfileResponse>build()
                .withData(fullProfile)
                .withMessage("Success"));
    }
}
