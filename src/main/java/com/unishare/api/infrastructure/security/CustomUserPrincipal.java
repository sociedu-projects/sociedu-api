package com.unishare.api.infrastructure.security;

import com.unishare.api.common.constants.Capabilities;
import com.unishare.api.common.constants.Roles;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Custom UserDetails implementation holding userId, email, roles, and capabilities.
 */
@Getter
public class CustomUserPrincipal implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String passwordHash;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public CustomUserPrincipal(UUID userId,
                               String email,
                               String passwordHash,
                               List<String> roles,
                               List<String> capabilities,
                               boolean enabled) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;

        List<GrantedAuthority> auths = new java.util.ArrayList<>();
        roles.forEach(r -> auths.add(new SimpleGrantedAuthority("ROLE_" + r)));
        capabilities.forEach(c -> auths.add(new SimpleGrantedAuthority(c)));
        if (hasAdminAccess(roles, capabilities)) {
            auths.add(new SimpleGrantedAuthority(Capabilities.VIEW_PROFILE));
            auths.add(new SimpleGrantedAuthority(Capabilities.UPDATE_PROFILE));
        }
        this.authorities = java.util.Collections.unmodifiableList(auths);
    }

    private boolean hasAdminAccess(List<String> roles, List<String> capabilities) {
        boolean adminRole = roles.stream()
                .map(role -> role == null ? "" : role.trim().toUpperCase(Locale.ROOT))
                .anyMatch(Roles.ADMIN::equals);
        boolean manageAll = capabilities.stream().anyMatch(Capabilities.MANAGE_ALL::equals);
        return adminRole || manageAll;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
