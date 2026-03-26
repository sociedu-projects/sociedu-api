package com.unishare.api.infrastructure.security;

import com.unishare.api.modules.auth.repository.CapabilityRepository;
import com.unishare.api.modules.auth.repository.UserCredentialRepository;
import com.unishare.api.modules.auth.repository.UserRepository;
import com.unishare.api.modules.auth.entity.User;
import com.unishare.api.modules.auth.entity.UserCredential;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final CapabilityRepository capabilityRepository;

    /**
     * Load by email — used by Spring Security's DaoAuthenticationProvider.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return buildPrincipal(user);
    }

    /**
     * Load by userId — used by JwtAuthenticationFilter after token validation.
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        return buildPrincipal(user);
    }

    private CustomUserPrincipal buildPrincipal(User user) {
        UserCredential credential = userCredentialRepository.findByUserId(user.getId())
                .orElse(null);

        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .toList();

        List<String> capabilities = capabilityRepository.findCapabilityNamesByUserId(user.getId());

        boolean enabled = "active".equalsIgnoreCase(user.getStatus());

        return new CustomUserPrincipal(
                user.getId(),
                user.getEmail(),
                credential != null ? credential.getPasswordHash() : null,
                roles,
                capabilities,
                enabled
        );
    }
}
