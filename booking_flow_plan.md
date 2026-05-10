# Kế Hoạch Triển Khai Hoàn Thiện Booking Flow (Enterprise-Ready)

Bản kế hoạch này đã được tinh chỉnh đạt chuẩn **Production-ready / Marketplace MVP**, tập trung xử lý triệt để: Idempotency, Race Condition, Payout Safety và Domain Invariants.

---

## STEP 1: Foundation (Nền tảng & Bảo vệ)

Đây là bước BẮT BUỘC trước khi code bất kỳ Business Flow nào để đảm bảo hệ thống an toàn ở môi trường concurrency cao.

- [ ] **Optimistic Locking**: Bổ sung trường `@Version private Long version;` vào các entity cốt lõi (`Booking`, `BookingSession`, `PayoutRecord`) để chặn triệt để rủi ro Race Condition (Ví dụ: Mentor bấm `COMPLETE` cùng lúc Mentee bấm `REPORT NO_SHOW`).
- [ ] **Centralized State Machine**: 
   - Thay vì `if-else` rải rác, xây dựng Enum/Map trung tâm để kiểm soát luồng chuyển trạng thái hợp lệ. Ví dụ: `BookingStatusTransitionPolicy`, `SessionStatusTransitionPolicy`.
   - Hàm `validateTransition(from, to)` sẽ ném `InvalidStateException` nếu cố tình đi tắt (vd: `CANCELED` -> `COMPLETED`).
- [ ] **Payout Idempotency Constraint**: Thiết lập `UNIQUE(booking_id)` (hoặc `event_id`) cho bảng `PayoutRecord` ở tầng Database để Database ngăn chặn double-payout.
- [ ] **Transactional Event Listener**: Cấu hình Spring Cloud / Spring Data Event: Dùng `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` để bảo đảm Event (vd: `BookingCompletedEvent`) CHỈ được publish khi DB Transaction đã commit thành công.

---

## STEP 2: Scheduling Flow (Lên lịch & Validate)

- [ ] **Role-based Validation Matrix**:
   - Chỉ định rõ ràng: Mentor mới được đổi `scheduled_at`, `meeting_url` và update thành `COMPLETED`.
   - Mentee không được can thiệp vào link học hoặc tự ấn `COMPLETED`. (Sử dụng 2 DTO khác nhau: `MentorUpdateSessionRequest` và `MenteeUpdateSessionRequest`).
- [ ] **Auto-transition to SCHEDULED / IN_PROGRESS**:
   - `Session` tự đổi `SCHEDULED` khi có lịch.
   - `Booking` tự đổi `IN_PROGRESS` khi có ít nhất 1 buổi bắt đầu (chưa cần `COMPLETED`).
- [ ] **Domain Invariants**: Thêm các trường Audit quan trọng: `actualStartedAt` và `actualEndedAt`.
- [ ] **Notifications**: Kích hoạt `SessionScheduledEvent` (với `@TransactionalEventListener`).

---

## STEP 3: Completion Flow (Hoàn thành & Tiền tệ)

- [ ] **Strict COMPLETED Validation**: 
   - `now >= scheduledAt + minimumDuration` (Tránh spam click).
   - Require: `actualStartedAt != null`. 
   - Tự động set `actualEndedAt = now` khi đánh dấu hoàn thành.
- [ ] **Booking Completion Check**: Hàm private kiểm tra toàn bộ sessions. Nếu ALL == `COMPLETED`, chuyển Booking sang `COMPLETED`.
- [ ] **Payout Record Creation**: Lắng nghe `BookingCompletedEvent` -> Insert một dòng `PayoutRecord` (trạng thái `PENDING`) duy nhất. Tuyệt đối không gọi API ngân hàng/ví tại đây. (Async Worker sẽ làm việc này sau cùng Retry Policy).
- [ ] **Chat Room Idempotency**: Khi lắng nghe `BookingCreatedEvent` để tạo Group Chat, luôn check `existsByBookingId` trước khi tạo.

---

## STEP 4: Cancel & Dispute Flow (Xử lý Ngoại lệ)

*Phase này sẽ áp dụng thiết kế Event-driven và Aggregate Strategies thay vì code gộp.*

- [ ] **Lưu dấu vết Hủy (Audit)**: Thêm `canceledBy`, `canceledAt`, `cancelReason` cho các Record bị hủy.
- [ ] **Event-Driven Refund**: Khi có tranh chấp/Hủy, Booking service không tự refund. Nó sẽ bắn ra `BookingCanceledEvent(reason)`. Sẽ có `RefundService` riêng lắng nghe và dựa vào `RefundPolicy` (thời điểm hủy, lỗi do ai) để xử lý hoàn tiền.
- [ ] **Timeout / Stale Job**: (Có thể làm sau) Viết Spring Scheduler quét các Booking `PENDING` quá 7 ngày để tự động giải tán hoặc gửi cảnh báo.

---

## Đề xuất lộ trình Coding

Chúng ta sẽ bám sát 4 STEPs trên, đi từ móng lên mái:
1. **Tiến hành STEP 1 ngay lập tức**: Setup các `@Version`, tạo Validator State Machine và chuyển các Event Listener sang `AFTER_COMMIT`.
