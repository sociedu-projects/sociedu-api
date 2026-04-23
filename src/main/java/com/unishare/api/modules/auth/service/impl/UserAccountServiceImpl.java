package com.unishare.api.modules.auth.service.impl;

import com.unishare.api.common.constants.Roles;
import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.auth.dto.UserAccountBrief;
import com.unishare.api.modules.auth.entity.Role;
import com.unishare.api.modules.auth.entity.User;
import com.unishare.api.modules.auth.entity.UserRole;
import com.unishare.api.modules.auth.exception.AuthErrorCode;
import com.unishare.api.modules.auth.repository.RoleRepository;
import com.unishare.api.modules.auth.repository.UserRepository;
import com.unishare.api.modules.auth.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserAccountBrief> listAccounts() {
        return userRepository.findAll().stream()
                .map(this::toBrief)
                .toList();
    }

    @Override
    @Transactional
    public UserAccountBrief replaceSingleRole(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND, "User not found"));

        String normalizedRole = normalizeRole(roleName);
        Role role = roleRepository.findByName(normalizedRole)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND, "Role not found: " + normalizedRole));

        user.getUserRoles().clear();
        UserRole userRole = new UserRole();
        userRole.setRole(role);
        userRole.getId().setRoleId(role.getId());
        user.addUserRole(userRole);
        userRole.getId().setUserId(user.getId());

        User saved = userRepository.save(user);
        return toBrief(saved);
    }

    private String normalizeRole(String roleName) {
        String normalized = Roles.normalizePrincipalRoleName(roleName);
        if (!Roles.isPrincipalAssignable(normalized)) {
            throw new AppException(AuthErrorCode.ACCESS_DENIED, "Unsupported role: " + roleName);
        }
        return normalized;
    }

    private UserAccountBrief toBrief(User user) {
        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .toList();
        return new UserAccountBrief(
                user.getId(),
                user.getEmail(),
                user.getStatus(),
                user.getCreatedAt(),
                roles
        );
    }
}
