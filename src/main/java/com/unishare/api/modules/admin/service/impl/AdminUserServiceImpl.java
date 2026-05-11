package com.unishare.api.modules.admin.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.admin.dto.AdminUserSummaryResponse;
import com.unishare.api.modules.admin.service.AdminUserService;
import com.unishare.api.modules.auth.dto.UserAccountBrief;
import com.unishare.api.modules.auth.entity.User;
import com.unishare.api.modules.auth.exception.AuthErrorCode;
import com.unishare.api.modules.auth.repository.UserRepository;
import com.unishare.api.modules.auth.service.UserAccountService;
import com.unishare.api.modules.user.dto.UserProfileNames;
import com.unishare.api.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserAccountService userAccountService;
    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserSummaryResponse> listUsers() {
        List<UserAccountBrief> briefs = userAccountService.listAccounts();
        Map<UUID, UserProfileNames> names = userService.getProfileNamesByUserIds(
                briefs.stream().map(UserAccountBrief::userId).toList());
        return briefs.stream()
                .map(b -> toSummary(b, names.get(b.userId())))
                .toList();
    }

    @Override
    @Transactional
    public AdminUserSummaryResponse updateUserRole(UUID userId, String roleName) {
        UserAccountBrief b = userAccountService.replaceSingleRole(userId, roleName);
        UserProfileNames pn = userService.getProfileNamesByUserIds(List.of(b.userId())).get(b.userId());
        return toSummary(b, pn);
    }

    @Override
    @Transactional
    public AdminUserSummaryResponse updateUserStatus(UUID userId, String status) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND, "User not found"));
        user.setStatus(status.toLowerCase());
        User saved = userRepository.save(user);
        UserProfileNames pn = userService.getProfileNamesByUserIds(List.of(saved.getId())).get(saved.getId());
        return toSummary(toBrief(saved), pn);
    }

    private UserAccountBrief toBrief(User user) {
        return new UserAccountBrief(
                user.getId(),
                user.getEmail(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUserRoles().stream().map(ur -> ur.getRole().getName()).toList()
        );
    }

    private AdminUserSummaryResponse toSummary(UserAccountBrief b, UserProfileNames pn) {
        return AdminUserSummaryResponse.builder()
                .userId(b.userId())
                .email(b.email())
                .profile(pn != null ? pn : UserProfileNames.EMPTY)
                .status(b.status())
                .createdAt(b.createdAt())
                .roles(b.roles())
                .build();
    }
}
