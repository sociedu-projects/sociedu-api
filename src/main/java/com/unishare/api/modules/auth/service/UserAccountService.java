package com.unishare.api.modules.auth.service;

import com.unishare.api.modules.auth.dto.UserAccountBrief;

import java.util.List;
import java.util.UUID;

/**
 * Thao tác tài khoản / role trong phạm vi auth — để admin và các module khác không chạm trực tiếp {@code UserRepository}.
 */
public interface UserAccountService {

    List<UserAccountBrief> listAccounts();

    UserAccountBrief replaceSingleRole(UUID userId, String roleName);
}
