# Unishare API — System Overview

> **Đọc file này trước khi làm bất kỳ task nào.** Đây là tài liệu tổng quan hệ thống để AI Agent hiểu context nhanh nhất.

---

## 1. Unishare là gì?

Marketplace kết nối **Mentee** (người học) với **Mentor** (người hướng dẫn). Mentee tìm kiếm và mua các gói tư vấn từ Mentor, thực hiện thanh toán online, và tham gia các buổi học được lên lịch. Hệ thống đảm bảo chất lượng dịch vụ qua cơ chế bằng chứng buổi học, báo cáo vi phạm và giải quyết tranh chấp.

---

## 2. Các Actor trong hệ thống

| Actor | Mô tả |
|---|---|
| **MENTEE** | Người học, mua gói dịch vụ, đặt lịch học |
| **MENTOR** | Người dạy, tạo gói dịch vụ, quản lý buổi học |
| **ADMIN** | Quản trị viên, kiểm duyệt, giải quyết tranh chấp |

---

## 3. Bản đồ Module (10 Module)

| Module | Trách nhiệm |
|---|---|
| `auth` | Đăng ký, đăng nhập, JWT, OTP, phân quyền RBAC |
| `user` | Hồ sơ cá nhân, học vấn, kinh nghiệm, chứng chỉ |
| `service` | Gói dịch vụ của Mentor (Package, Version, Curriculum) |
| `order` | Đơn hàng mua gói dịch vụ và vòng đời trạng thái |
| `payment` | Tích hợp cổng thanh toán (VNPay/MoMo), xử lý Webhook |
| `booking` | Lên lịch buổi học, theo dõi tiến độ, bằng chứng hoàn thành |
| `chat` | Nhắn tin thời gian thực giữa các bên tham gia |
| `dispute` | Khiếu nại, phân xử tranh chấp, kết nối hoàn tiền |
| `file` | Lưu trữ và phục vụ file tập trung (avatar, tài liệu, bằng chứng) |
| `report` | Báo cáo vi phạm nội dung và người dùng |

---

## 4. Luồng Nghiệp Vụ Xuyên Module (Happy Path)

```
[MENTEE đăng ký]
  auth → (sự kiện USER_CREATED) → user (tạo profile trống)

[MENTEE mua dịch vụ]
  service (xem gói)
    → order (tạo đơn, status: pending_payment)
      → payment (sinh URL thanh toán)
        → [Cổng thanh toán]
          → payment (nhận Webhook, status: success)
            → order (status: paid)
              → booking (tạo booking + sessions)
                → chat (tạo phòng chat loại 'booking')

[Quá trình học]
  booking (lên lịch session, cập nhật meeting_url)
    → booking (tải bằng chứng lên → file)
      → booking (hoàn thành session → hoàn thành booking)
        → order (status: completed, kích hoạt giải ngân)

[Tranh chấp]
  dispute (Mentee khiếu nại → Admin xét duyệt)
    → order/payment (hoàn tiền hoặc giải ngân)
```

---

## 5. Quan hệ Phụ thuộc Module

```
auth ──────────────────────────────────► user (cung cấp UserId)
user ──────────────────────────────────► service, booking, chat (cung cấp profile)
service ───────────────────────────────► order (cung cấp giá PackageVersion)
order ─────────────────────────────────► payment (yêu cầu/nhận kết quả thanh toán)
order ─────────────────────────────────► booking (kích hoạt khi paid)
booking ───────────────────────────────► chat (tạo phòng chat)
booking ───────────────────────────────► file (lưu bằng chứng)
dispute ───────────────────────────────► booking, order, payment (phân xử)
report ────────────────────────────────► user, dispute (leo thang vi phạm)
file ──────────────────────────────────► (được gọi bởi: user, booking, chat, report)
```

> **Quy tắc cứng**: Module A KHÔNG ĐƯỢC inject Repository của Module B. Phải gọi qua Service Interface.

---

## 6. Cấu trúc Package chuẩn mỗi Module

```
modules/{module}/
  ├── controller/      # REST endpoints — chỉ route và validate DTO
  ├── service/         # Interface nghiệp vụ
  │   └── impl/        # Implementation chứa toàn bộ business logic
  ├── repository/      # Spring Data JPA
  ├── entity/          # JPA entities — ánh xạ DB table
  ├── dto/             # Request / Response payloads
  ├── mapper/          # MapStruct hoặc static factory
  └── MODULE_FLOW.md   # Đọc file này để hiểu module
```

---

## 7. Stack Kỹ thuật

- **Runtime**: Java 21, Spring Boot 3.x
- **ORM**: Spring Data JPA + Hibernate
- **DB**: PostgreSQL
- **Auth**: JWT (Stateless) + BCrypt
- **API Docs**: SpringDoc OpenAPI (Swagger UI)
- **Testing**: JUnit 5 + Mockito + Testcontainers
- **Build**: Gradle (Kotlin DSL)

---

## 8. Quy tắc Agent khi làm task

1. Đọc `SYSTEM.md` (file này) → hiểu hệ thống
2. Đọc `MODULE_FLOW.md` của module liên quan → hiểu nghiệp vụ
3. Đọc `SECURITY.md` → **bắt buộc** nếu task liên quan đến phân quyền, endpoint mới, hoặc RBAC
4. Đọc `.agents/workflows/instruction.md` → quy chuẩn code chi tiết
5. Đọc `.agents/workflows/skill.md` → kỹ thuật nâng cao nếu cần
6. Xem code hiện tại trong module trước khi sinh code mới
