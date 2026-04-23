package com.unishare.api.modules.admin.service;

import com.unishare.api.modules.admin.dto.AdminUserSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface AdminUserService {

    List<AdminUserSummaryResponse> listUsers();

    AdminUserSummaryResponse updateUserRole(UUID userId, String roleName);
}
