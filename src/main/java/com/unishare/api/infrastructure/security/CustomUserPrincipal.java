package com.unishare.api.infrastructure.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetails implementation holding userId, email, roles, and capabilities.
 */
@Getter
public class CustomUserPrincipal implements UserDetails {

    private final Long userId;
    private final String email;
    private final String passwordHash;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public CustomUserPrincipal(Long userId,
                               String email,
                               String passwordHash,
                               List<String> roles,
                               List<String> capabilities,
                               boolean enabled) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;

        // Combine ROLE_ prefixed roles AND plain capability strings as authorities
        List<GrantedAuthority> auths = new java.util.ArrayList<>();
        roles.forEach(r -> auths.add(new SimpleGrantedAuthority("ROLE_" + r)));
        capabilities.forEach(c -> auths.add(new SimpleGrantedAuthority(c)));
        this.authorities = java.util.Collections.unmodifiableList(auths);
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
