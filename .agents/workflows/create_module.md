---
description: Hướng dẫn luồng các bước khi tạo một Module MỚI trong dự án
---
# Tạo Module Mới

1. Tạo một thư mục mới trong `src/main/java/com/unishare/api/modules/` (Ví dụ: `inventory`).
2. Bên trong thư mục module mới, tạo BẮT BUỘC đầy đủ các package: `controller`, `service`, `repository`, `dto`, `entity`, `mapper`.
3. Bắt đầu với `entity`: Tạo JPA Entity đại diện cho domain bằng cách cấu hình các annotation `@Entity`, `@Table`.
4. Tạo `repository`: Interface kế thừa từ `JpaRepository<Entity, Id>`.
5. Tạo `dto`: Phân tách rõ ràng Request DTO và Response DTO (ví dụ `InventoryCreateRequest`, `InventoryResponse`).
6. Tạo `mapper`: Class hoặc Interface chịu trách nhiệm chuyển đổi qua lại giữa Entity và DTO.
7. Tạo `service`: Viết interface (ví dụ `InventoryService`) định nghĩa các use-case, sau đó viết class `InventoryServiceImpl` (có gắn `@Service`) thực thi logic và dùng `@RequiredArgsConstructor` để inject `repository`.
8. Tạo `controller`: Cung cấp điểm vào REST (ví dụ `@RestController`), map các HTTP method, gọi `service` và trả về kết quả được bao bọc trong Generic Response.
9. Đảm bảo KHÔNG bypass qua Service để gọi thẳng Repository từ Controller.
