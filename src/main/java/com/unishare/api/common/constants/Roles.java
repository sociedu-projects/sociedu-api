package com.unishare.api.common.constants;

import java.util.Locale;
import java.util.Set;

/**
 * Giá trị cột {@code roles.name} — khớp seed / Flyway. Dùng thống nhất cho validation, JWT, admin.
 */
public final class Roles {

    private Roles() {}

    public static final String USER = "USER";
    public static final String MENTOR = "MENTOR";
    public static final String ADMIN = "ADMIN";

    /**
     * Nhãn từ vựng product (vd. "buyer"); trong DB hiện tại đăng ký thường gán {@link #USER}.
     */
    public static final String BUYER = "BUYER";

    /**
     * Role chính có thể gán qua API admin (đổi role tài khoản). Phải khớp {@link #API_PRINCIPAL_ASSIGNABLE_PATTERN}.
     */
    public static final Set<String> PRINCIPAL_ASSIGNABLE = Set.of(USER, MENTOR, ADMIN);

    /**
     * Bean Validation / OpenAPI: một trong các role principal (không phân biệt hoa thường).
     */
    public static final String API_PRINCIPAL_ASSIGNABLE_PATTERN = "(?i)^(USER|MENTOR|ADMIN)$";

    public static String normalizePrincipalRoleName(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean isPrincipalAssignable(String normalizedUpperCase) {
        return PRINCIPAL_ASSIGNABLE.contains(normalizedUpperCase);
    }
}
