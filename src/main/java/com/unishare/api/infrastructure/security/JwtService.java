package com.unishare.api.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationDays;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${app.jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    /**
     * Generate a signed Access Token containing userId and roles.
     * Subject is the canonical string form of {@link UUID}.
     */
    public String generateAccessToken(UUID userId, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpirationMs)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate a random opaque refresh token (UUID). The actual token is stored in DB.
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Returns the expiry instant for the refresh token.
     */
    public Instant getRefreshTokenExpiry() {
        return Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS);
    }

    /**
     * Returns the expiry in seconds for the access token (for AuthResponse).
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }

    /**
     * Validate the JWT and return the userId ("sub" claim).
     *
     * @throws JwtException if invalid or expired
     */
    public UUID extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Returns true if the token is valid (not expired, correct signature).
     */
    public boolean isTokenValid(String token) {
        try {
            extractUserId(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JWT] Invalid token: {}", e.getMessage());
            return false;
        }
    }
}
