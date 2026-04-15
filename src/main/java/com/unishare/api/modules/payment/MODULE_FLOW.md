# Module: Payment — Thanh toán

Tích hợp cổng thanh toán bên thứ ba (VNPay, MoMo) để xử lý giao dịch cho các đơn hàng.

## Entities
- `payment_transactions` — chi tiết giao dịch (order_id, provider, provider_transaction_id, amount, status, raw_response)
- `payment_status` (enum) — `pending` | `success` | `failed`

## Core Flows

**Khởi tạo thanh toán**: Nhận `orderId` từ Module `order` → tạo `payment_transactions` (status=`pending`) → gọi API Provider để lấy payment URL → trả URL về client để chuyển hướng.

**Xử lý Webhook**: Provider gọi vào endpoint Webhook → xác minh chữ ký (HMAC/SHA256) → lookup transaction theo `provider_transaction_id` → cập nhật status (`success`/`failed`) + lưu `raw_response` → thông báo kết quả cho Module `order`.

**Đối soát (Reconciliation)**: Job định kỳ quét transaction `pending` quá 15 phút → chủ động query trạng thái từ Provider API → cập nhật trạng thái cuối cùng.

## Quan hệ Module
- **Nhận yêu cầu** từ Module `order` để tạo giao dịch.
- **Thông báo kết quả** về Module `order` sau khi xử lý Webhook.
- **Phụ thuộc** Provider API bên ngoài (VNPay, MoMo).

## Business Rules
- **Bắt buộc** xác minh chữ ký Webhook trước khi xử lý. Từ chối mọi request không hợp lệ (HTTP 400).
- **Idempotency**: `provider_transaction_id` là unique key — cùng 1 giao dịch từ Provider chỉ được xử lý đúng 1 lần.
- Lưu toàn bộ `raw_response` từ Provider vào cột JSONB để phục vụ đối soát sau này.
- Không lưu bất kỳ thông tin thẻ/tài khoản ngân hàng của user. Mọi dữ liệu nhạy cảm nằm ở Provider.
