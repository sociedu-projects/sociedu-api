# Module: Booking — Lên lịch & Tổ chức Buổi học

Quản lý toàn bộ quá trình học tập sau khi đơn hàng được thanh toán: lên lịch, xác nhận, ghi nhận hoàn thành và thu thập bằng chứng.

## Entities
- `bookings` — đợt học (order_id unique, buyer_id, mentor_id, package_id, status)
- `booking_sessions` — từng buổi học (booking_id, curriculum_id, scheduled_at, status, meeting_url)
- `booking_session_evidences` — bằng chứng hoàn thành (session_id, file_id, uploaded_by)
- `booking_status` (enum) — `pending` | `scheduled` | `in_progress` | `completed` | `canceled` | `refunded`
- `session_status` (enum) — `pending` | `scheduled` | `completed` | `no_show` | `canceled`

## Core Flows

**Khởi tạo Booking**: Module `order` kích hoạt khi status=`paid` → tạo `bookings` (status=`pending`) → dựa vào `PackageCurriculum` sinh ra N `booking_sessions` tương ứng (status=`pending`).

**Lên lịch Session**: Mentor đề xuất thời gian `scheduled_at` + `meeting_url` → Mentee xác nhận → session chuyển sang `scheduled` → booking chuyển sang `scheduled`/`in_progress`.

**Hoàn thành Session**: Sau giờ học → Mentor/Mentee tải lên `booking_session_evidences` (file bằng chứng) → Session chuyển `completed` → nếu toàn bộ sessions `completed` thì Booking → `completed` → kích hoạt giải ngân trong Module `order`.

**Vắng mặt/Hủy Session**: Session chuyển `no_show` hoặc `canceled` → ghi nhận lý do → có thể dẫn đến khởi tạo tranh chấp.

## Quan hệ Module
- **Được kích hoạt** bởi Module `order` (sau paid).
- **Đọc** cấu trúc Curriculum từ Module `service`.
- **Gọi** Module `file` để lưu bằng chứng.
- **Kích hoạt** Module `chat` tạo phòng chat loại `booking`.
- **Kích hoạt** Module `order` cập nhật status khi Booking hoàn thành.

## Business Rules
- Một `order_id` chỉ có đúng 1 `booking` (unique constraint).
- Chỉ Mentor và Mentee trong booking mới có quyền đọc/sửa sessions (kiểm tra `buyer_id`/`mentor_id`).
- Session chỉ được chuyển sang `completed` sau thời điểm `scheduled_at`.
- Bằng chứng (`evidence`) được upload bởi cả Mentor lẫn Mentee (`uploaded_by`).
