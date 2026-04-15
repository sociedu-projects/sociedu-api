package com.unishare.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_JWT = "bearer-jwt";

    @Bean
    public OpenAPI unishareOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Unishare API")
                        .description(INFO_DESCRIPTION)
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT, new SecurityScheme()
                                .name(BEARER_JWT)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token (Authorization: Bearer ...)")));
    }

    /**
     * Mô tả luồng nghiệp vụ tổng thể (Swagger UI — phần đầu trang).
     */
    private static final String INFO_DESCRIPTION = """
            REST API nền tảng kết nối mentee–mentor. Phần lớn endpoint yêu cầu JWT: bấm **Authorize**, dán \
            `accessToken` nhận từ `POST /api/v1/auth/login` (hoặc `/refresh`). Các nhóm public (đăng ký, đăng nhập, \
            callback VNPay, v.v.) không cần gửi Bearer.

            ### Luồng xác thực
            1. `POST /api/v1/auth/register` — tạo tài khoản, email nhận OTP.
            2. Xác minh email (`/api/v1/auth/verify-email`, `/api/v1/auth/resend-verification`).
            3. `POST /api/v1/auth/login` — lấy access + refresh token; client gửi `Authorization: Bearer <access>`.
            4. `POST /api/v1/auth/refresh` — đổi refresh lấy bộ token mới; `POST /api/v1/auth/logout` — thu hồi refresh.

            ### Luồng mentor & gói dịch vụ
            Mentor cập nhật hồ sơ và tạo gói (`/api/v1/mentors/me`, packages, curriculum). Mentee xem danh bạ \
            mentor và gói (`GET /api/v1/mentors`, `/api/v1/mentors/{id}/packages`) rồi chọn mua.

            ### Luồng thanh toán & đơn hàng
            1. `POST /api/v1/orders/checkout` — tạo đơn `pending_payment`, nhận `paymentUrl` VNPay.
            2. Người dùng thanh toán trên VNPay; hệ thống nhận `GET /api/v1/payments/vnpay/ipn` (server-to-server) và \
            `GET /api/v1/payments/vnpay/return` (redirect trình duyệt). Có thể tra cứu `GET /api/v1/payments/order/{orderId}`.
            3. Khi thanh toán thành công, luồng domain tạo/cập nhật booking (xem nhóm **Bookings**).

            ### Luồng sau mua — booking & báo cáo
            - **Bookings**: mentee/mentor xem phiên, cập nhật session, đính kèm minh chứng buổi học.
            - **Progress reports**: mentee nộp báo cáo (`/api/v1/mentee/reports`), mentor phản hồi (`/api/v1/mentors/me/reports`).

            ### Luồng tin cậy & khiếu nại
            **Trust**: tạo báo cáo vi phạm, thêm bằng chứng; giải quyết báo cáo/tranh chấp (endpoint resolve thường \
            yêu cầu quyền admin/moderator theo `Capabilities`).

            ### File & chat
            Upload file (`multipart`) để dùng trong hồ sơ hoặc minh chứng. Chat: tạo hội thoại, gửi/nhận tin theo \
            conversation.

            ### Gợi ý thử trên Swagger UI
            Gọi `login` → copy `accessToken` → **Authorize** → thử các nhóm còn lại theo thứ tự nghiệp vụ trên.""";
}
