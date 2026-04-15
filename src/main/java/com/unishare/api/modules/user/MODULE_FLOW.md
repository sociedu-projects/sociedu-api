# Module: User — Hồ sơ Người dùng

Quản lý thông tin cá nhân, học vấn, kinh nghiệm, chứng chỉ và ngoại ngữ của tất cả người dùng (Mentee & Mentor).

## Entities
- `user_profiles` — thông tin cá nhân cơ bản (tên, bio, headline, location, avatar_file_id)
- `user_educations` — lịch sử học vấn (liên kết `universities`, `fields_of_study`)
- `user_experiences` — kinh nghiệm làm việc / hoạt động
- `user_certificates` — chứng chỉ & bằng cấp (liên kết file minh chứng)
- `user_languages` — ngoại ngữ và trình độ
- `universities`, `fields_of_study` — danh mục tham chiếu (lookup tables)

## Core Flows

**Khởi tạo Profile**: Khi Module `auth` tạo user mới → tự động tạo `user_profiles` trống gắn với `user_id`.

**Cập nhật Profile**: User cập nhật họ tên, bio, headline → validate → lưu. Nếu upload avatar mới → gọi Module `file` lấy `file_id` → lưu vào `avatar_file_id`.

**Cập nhật Học vấn/Kinh nghiệm**: User thêm/sửa/xóa Education hoặc Experience → validate logic thời gian (`start_date ≤ end_date`) → lưu.

**Upload Chứng chỉ**: User tải file chứng chỉ → gọi Module `file` → lưu `credential_file_id` vào `user_certificates`.

## Quan hệ Module
- **Nhận sự kiện** từ Module `auth` để tạo profile trống.
- **Gọi** Module `file` để lưu avatar và chứng chỉ.
- **Cung cấp** thông tin hiển thị (tên, avatar) cho `booking`, `chat`, `service`.

## Business Rules
- User chỉ được sửa profile của **chính mình** (kiểm tra `userId` từ token).
- `start_date` không được lớn hơn `end_date` trong Education/Experience.
- Khi `is_current = true`, `end_date` phải để null.
- Chỉ có Admin mới được xem hoặc sửa profile của người dùng khác.
