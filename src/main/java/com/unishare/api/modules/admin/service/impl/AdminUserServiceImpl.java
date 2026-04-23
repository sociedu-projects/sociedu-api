package com.unishare.api.modules.admin.service.impl;

import com.unishare.api.modules.admin.dto.AdminUserSummaryResponse;
import com.unishare.api.modules.admin.service.AdminUserService;
import com.unishare.api.modules.auth.dto.UserAccountBrief;
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
