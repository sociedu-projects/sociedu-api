# Module: Order — Đơn hàng

Quản lý vòng đời giao dịch khi Mentee mua gói dịch vụ của Mentor.

## Entities
- `orders` — đơn hàng chính (buyer_id, service_id, total_amount, status, paid_at)
- `order_status` (enum) — `pending_payment` | `paid` | `failed` | `canceled` | `refunded`

## Core Flows

**Tạo đơn hàng**: Mentee chọn gói → hệ thống lấy giá từ `ServicePackageVersion.is_default` (KHÔNG tin giá từ client) → tạo `orders` với status `pending_payment` → trả về `orderId` để chuyển sang đặt thanh toán.

**Xác nhận thanh toán**: Module `payment` callback kết quả → nếu success: update status → `paid`, ghi `paid_at` → kích hoạt Module `booking` tạo booking + sessions.

**Hủy đơn**: Status chỉ có thể chuyển sang `canceled` khi còn ở `pending_payment` và chưa tạo payment transaction.

**Hoàn tiền**: Module `dispute` ra lệnh hoàn tiền → update status → `refunded` → phối hợp Module `payment` thực thi lệnh hoàn.

## Quan hệ Module
- **Gọi** Module `service` để lấy giá gói dịch vụ hiện hành.
- **Gọi** Module `payment` để thực hiện giao dịch.
- **Kích hoạt** Module `booking` sau khi status = `paid`.
- **Nhận lệnh** từ Module `dispute` để xử lý hoàn tiền.

## Business Rules
- `total_amount` phải được tính server-side. Tuyệt đối không nhận giá từ request body.
- State machine cứng: chỉ được chuyển trạng thái theo chiều `pending_payment → paid/failed/canceled`, `paid → refunded`.
- Buyer chỉ thấy đơn của **chính mình**. Mentor chỉ thấy đơn hàng khách đặt gói của **mình**.
- Không cho phép đặt mua gói đang `is_active=false`.
