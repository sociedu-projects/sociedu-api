# Module: Service — Gói Dịch vụ Mentor

Quản lý các gói tư vấn do Mentor tạo ra, bao gồm thông tin giá, thời lượng và lộ trình học.

## Entities
- `service_packages` — gói dịch vụ cơ bản (mentor_id, tên, mô tả, is_active)
- `service_package_versions` — phiên bản giá & thời lượng (price, duration, delivery_type, is_default)
- `package_curriculums` — lộ trình nội dung theo thứ tự (title, description, order_index, duration)

## Core Flows

**Tạo gói dịch vụ**: Mentor (đã có role MENTOR) tạo `service_packages` → thêm ít nhất 1 `service_package_versions` (is_default=true) → định nghĩa `package_curriculums` theo thứ tự.

**Cập nhật giá**: Khi thay đổi giá → tạo `service_package_versions` mới → đánh dấu `is_default=true` → version cũ giữ nguyên để đơn hàng cũ không bị ảnh hưởng.

**Bật/tắt gói**: Mentor set `is_active=false` để tạm ngưng nhận đơn. Gói tắt không hiển thị trong kết quả tìm kiếm.

**Xem danh sách**: Mentee tìm kiếm gói theo tên/giá → hệ thống chỉ trả về gói có `is_active=true` kèm version `is_default=true`.

## Quan hệ Module
- **Cung cấp** thông tin giá (`ServicePackageVersion`) cho Module `order` khi đặt mua.
- **Cung cấp** cấu trúc Curriculum cho Module `booking` để khởi tạo sessions.
- **Xác minh** `mentor_id` qua Module `user` để hiển thị profile mentor đi kèm gói.

## Business Rules
- Chỉ user có role `MENTOR` mới được tạo/sửa gói dịch vụ.
- Mentor chỉ được sửa gói của **chính mình** (kiểm tra `mentor_id == currentUserId`).
- `price` phải ≥ 0. `duration` (số buổi/ngày) phải ≥ 1.
- Mỗi gói phải có đúng 1 version `is_default=true` tại mọi thời điểm.
- `order_index` trong Curriculum phải duy nhất trong cùng 1 version, bắt đầu từ 1.
