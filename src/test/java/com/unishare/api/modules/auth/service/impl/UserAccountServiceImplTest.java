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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserAccountServiceImpl userAccountService;

    private User sampleUser;
    private Role sampleRole;

    @BeforeEach
    void setUp() {
        sampleRole = new Role();
        sampleRole.setId(UUID.randomUUID());
        sampleRole.setName(Roles.USER);

        sampleUser = new User();
        sampleUser.setId(UUID.randomUUID());
        sampleUser.setEmail("test@example.com");
        sampleUser.setStatus("ACTIVE");
        sampleUser.setCreatedAt(Instant.now());

        UserRole userRole = new UserRole();
        userRole.setRole(sampleRole);
        userRole.getId().setRoleId(sampleRole.getId());
        userRole.getId().setUserId(sampleUser.getId());
        sampleUser.addUserRole(userRole);
    }

    @Nested
    @DisplayName("listAccounts")
    class ListAccountsTests {

        @Test
        @DisplayName("Lấy danh sách account thành công")
        void listAccounts_Success() {
            when(userRepository.findAll()).thenReturn(List.of(sampleUser));

            List<UserAccountBrief> result = userAccountService.listAccounts();

            assertNotNull(result);
            assertEquals(1, result.size());
            UserAccountBrief brief = result.get(0);
            assertEquals(sampleUser.getId(), brief.userId());
            assertEquals(sampleUser.getEmail(), brief.email());
            assertTrue(brief.roles().contains(Roles.USER));
        }

        @Test
        @DisplayName("Trả về danh sách rỗng nếu không có user")
        void listAccounts_Empty() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserAccountBrief> result = userAccountService.listAccounts();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("replaceSingleRole")
    class ReplaceSingleRoleTests {

        @Test
        @DisplayName("Thay đổi role thành công")
        void replaceSingleRole_Success() {
            Role newRole = new Role();
            newRole.setId(UUID.randomUUID());
            newRole.setName(Roles.MENTOR);

            when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));
            when(roleRepository.findByName(Roles.MENTOR)).thenReturn(Optional.of(newRole));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserAccountBrief result = userAccountService.replaceSingleRole(sampleUser.getId(), "MENTOR");

            assertNotNull(result);
            assertEquals(1, result.roles().size());
            assertTrue(result.roles().contains(Roles.MENTOR));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Ném exception nếu user không tồn tại")
        void replaceSingleRole_UserNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

            AppException ex = assertThrows(AppException.class, () -> 
                userAccountService.replaceSingleRole(unknownId, "MENTOR")
            );

            assertEquals(AuthErrorCode.USER_NOT_FOUND, ex.getExceptionCode());
            verify(roleRepository, never()).findByName(anyString());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ném exception nếu role không tồn tại trong DB")
        void replaceSingleRole_RoleNotFound() {
            when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));
            when(roleRepository.findByName(Roles.MENTOR)).thenReturn(Optional.empty());

            AppException ex = assertThrows(AppException.class, () -> 
                userAccountService.replaceSingleRole(sampleUser.getId(), "MENTOR")
            );

            assertEquals(AuthErrorCode.USER_NOT_FOUND, ex.getExceptionCode());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ném exception nếu role không được phép gán")
        void replaceSingleRole_UnassignableRole() {
            when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));

            AppException ex = assertThrows(AppException.class, () -> 
                userAccountService.replaceSingleRole(sampleUser.getId(), "INVALID_ROLE")
            );

            assertEquals(AuthErrorCode.ACCESS_DENIED, ex.getExceptionCode());
            verify(roleRepository, never()).findByName(anyString());
            verify(userRepository, never()).save(any());
        }
    }
}
