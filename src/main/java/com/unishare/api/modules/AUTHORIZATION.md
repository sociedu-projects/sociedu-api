# Phân quyền API (Unishare)

Tài liệu mô tả **mô hình phân quyền cơ bản** và **ánh xạ endpoint → role / capability**.  
Triển khai: Spring Security (`SecurityFilterChain` + `@EnableMethodSecurity`) + JWT; `CustomUserPrincipal` nạp **roles** (`ROLE_*`) và **capabilities** (chuỗi quyền nghiệp vụ) từ DB.

## Khái niệm

| Thành phần | Mô tả |
|------------|--------|
| **Role** | Nhóm người dùng: `USER` / `BUYER`, `MENTOR`, `ADMIN`, … — trong JWT/Security là `ROLE_<TÊN>` (vd. `ROLE_MENTOR`). |
| **Capability** | Quyền chi tiết (vd. `CREATE_PAYMENT`, `VIEW_BOOKING`) — dùng trong `@PreAuthorize("hasAuthority('...')")` hoặc `hasAuthority(T(com.unishare.api.common.constants.Capabilities).X)`. |
| **Endpoint công khai** | Không cần `Authorization: Bearer`; khai báo trong `SecurityConfig` (`permitAll`) và/hoặc `@PermitAll`. |
| **Chỉ cần đăng nhập** | `isAuthenticated()` — ownership (đúng user) xử lý thêm trong service. |

Giá trị role/capability khớp `Roles` / `Capabilities` và dữ liệu seed DB.

## Bảng endpoint (tóm tắt)

| Module | Endpoint | Yêu cầu |
|--------|----------|---------|
| Auth | `POST /register`, `/login`, `/refresh`, `/verify-email`, `/resend-verification`, `/forgot-password`, `/reset-password` | Công khai |
| Auth | `POST /logout` | Đã đăng nhập |
| Payment | `GET /vnpay/**` | Công khai (callback VNPay) |
| Payment | `GET /order/{orderId}` | `VIEW_PAYMENT` |
| Orders | `POST /checkout` | `CREATE_PAYMENT` |
| Orders | `GET /me`, `GET /{id}` | `VIEW_PAYMENT` |
| Bookings | `GET /me/buyer`, `GET /{id}` | `VIEW_BOOKING` |
| Bookings | `GET /me/mentor` | `VIEW_OWN_BOOKINGS` |
| Bookings | `PATCH .../sessions/...`, `POST .../evidences` | `MANAGE_SESSIONS` |
| Users | `GET /api/v1/users/{id}/profile` | Công khai |
| Users | `GET /api/v1/users/me/**` (đọc) | `VIEW_PROFILE` |
| Users | `PUT/POST/DELETE /api/v1/users/me/**` (ghi) | `UPDATE_PROFILE` |
| Mentors | `GET /api/v1/mentors`, `/{id}`, `/{id}/packages` | Công khai |
| Mentors | `PUT /me`, `POST/DELETE /me/**` | `ROLE_MENTOR` |
| Reports mentor | `/api/v1/mentors/me/reports/**` | `ROLE_MENTOR` |
| Reports mentee | `POST /api/v1/mentee/reports` | `CREATE_REPORT` |
| Reports mentee | `GET /api/v1/mentee/reports` | `VIEW_OWN_REPORT` |
| Trust | Báo cáo / tranh chấp (user) | `CREATE_REPORT`, `VIEW_OWN_REPORT`, `CREATE_DISPUTE`, `VIEW_OWN_DISPUTE` |
| Trust | `PUT .../reports/{id}/resolve` | `RESOLVE_REPORT` |
| Trust | `PUT .../disputes/{id}/resolve` | `RESOLVE_DISPUTE` |
| Chat | Tạo/list conversation, xem tin nhắn | `VIEW_CONVERSATION` |
| Chat | Gửi tin nhắn | `SEND_MESSAGE` |
| Files | `POST /upload` | `UPLOAD_ATTACHMENT` |
| Files | `GET/DELETE /{id}` | Đã đăng nhập (chi tiết quyền xem/xóa file trong service) |

## Ghi chú vận hành

- Đổi rule: sửa `@PreAuthorize` trên controller hoặc bổ sung `requestMatchers` trong `SecurityConfig`.
- Swagger: endpoint công khai dùng `@SecurityRequirements(value = {})` để không bắt buộc Bearer.
- Admin / mentor: đảm bảo user có đủ capability trong DB; thiếu quyền → HTTP 403 (`ApiAccessDeniedHandler`).
