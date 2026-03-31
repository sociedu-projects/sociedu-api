package com.unishare.api.modules.mentor.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.mentor.dto.MentorDto.*;
import com.unishare.api.modules.mentor.service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;

    // Public Mentor Directory
    @GetMapping
    public ResponseEntity<ApiResponse<List<MentorProfileResponse>>> getAllVerifiedMentors() {
        return ResponseEntity.ok(ApiResponse.<List<MentorProfileResponse>>build()
                .withData(mentorService.getAllVerifiedMentors()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> getMentorProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<MentorProfileResponse>build()
                .withData(mentorService.getMentorProfile(id)));
    }

    @GetMapping("/{id}/packages")
    public ResponseEntity<ApiResponse<List<ServicePackageResponse>>> getMentorPackages(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<List<ServicePackageResponse>>build()
                .withData(mentorService.getMentorPackages(id)));
    }

    // Mentor Management (for the mentor themselves)
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody MentorProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.<MentorProfileResponse>build()
                .withData(mentorService.createOrUpdateProfile(principal.getUserId(), request)));
    }

    @PostMapping("/me/packages")
    public ResponseEntity<ApiResponse<ServicePackageResponse>> addPackage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody ServicePackageRequest request) {
        return ResponseEntity.ok(ApiResponse.<ServicePackageResponse>build()
                .withData(mentorService.createPackage(principal.getUserId(), request)));
    }

    @DeleteMapping("/me/packages/{pkgId}")
    public ResponseEntity<ApiResponse<Void>> deletePackage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long pkgId) {
        mentorService.deletePackage(principal.getUserId(), pkgId);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Package deleted"));
    }
}
