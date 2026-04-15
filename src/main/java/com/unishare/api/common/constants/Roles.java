package com.unishare.api.common.constants;

/**
 * Giá trị cột {@code roles.name} — khớp seed / Flyway.
 */
public final class Roles {

    private Roles() {}

    public static final String USER = "USER";
    public static final String MENTOR = "MENTOR";
    public static final String ADMIN = "ADMIN";

    /**
     * Nhãn role trả về client/JWT khi đăng ký buyer (product có thể gọi là "buyer").
     */
    public static final String BUYER = "BUYER";
}
