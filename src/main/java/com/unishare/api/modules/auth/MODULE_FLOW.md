# Module: Auth — Xác thực & Phân quyền

Quản lý định danh người dùng, cấp JWT token và kiểm soát quyền truy cập theo mô hình RBAC (Role → Capability).

## Entities
- `users` — tài khoản cốt lõi (email, phone, status: pending/active/suspended)
- `user_credentials` — mật khẩu đã hash (BCrypt)
- `roles`, `capabilities`, `role_capabilities` — cấu hình RBAC
- `user_roles` — gán role cho user
- `refresh_tokens` — quản lý phiên đăng nhập
- `otp_tokens` — mã xác thực OTP (6 chữ số, có TTL)

## Core Flows

**Đăng ký**: Nhận email+password → validate trùng lặp → tạo `users` (status=`pending`) → hash password → sinh OTP → gửi email → User xác thực OTP → update status=`active`.

**Đăng nhập**: Kiểm tra email+password → user phải `active` → load Role+Capability → sinh Access Token (JWT) + Refresh Token → trả về.

**Làm mới Token**: Nhận Refresh Token hợp lệ/chưa hết hạn → sinh Access Token mới → trả về.

**Quên mật khẩu**: Nhận email → sinh OTP → gửi mail → User nhập OTP+mật khẩu mới → cập nhật `user_credentials`.

## Quan hệ Module
- **Cung cấp** `userId` + `authorities` (qua Spring Security Context) cho **toàn bộ module khác**.
- **Kích hoạt** Module `user` tạo `UserProfile` sau khi user đăng ký thành công.
- **Phụ thuộc** Infrastructure: MailService để gửi OTP.

## Business Rules
- User có status `pending` hoặc `suspended` bị từ chối đăng nhập (403).
- OTP hết hạn sau 5 phút (`expires_at`). OTP đã dùng không thể tái sử dụng (`used=true`).
- Refresh Token bị vô hiệu hoá ngay khi sử dụng (rotation). Token bị thu hồi khi `revoked=true`.
- Endpoint công khai: `/auth/register`, `/auth/login`, `/auth/verify-otp`, `/auth/forgot-password`.
