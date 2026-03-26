---
description: Các quy chuẩn về Dependency Injection, DTO, và xử lý Exception API
---

# Quy chuẩn API & Clean Code

1. **Dependency Injection**: 
   - Luôn sử dụng `@RequiredArgsConstructor` từ thư viện Lombok kết hợp với biến `private final` để Spring tự động inject bean.
   - Tuyệt đối không sử dụng `@Autowired` trực tiếp lên field để giúp unit test dễ dàng thực hiện (không phụ thuộc container).

2. **Xử lý Exception**:
   - Tầng Controller và Service không được dùng `try/catch` để sau đó trả ra `ResponseEntity` chứa chuỗi báo lỗi tĩnh.
   - Cần ném ra các ngoại lệ tùy chỉnh (Custom Exceptions - kế thừa từ `RuntimeException`).
   - Để `GlobalExceptionHandler` (`@RestControllerAdvice`) ở tầng Global bắt các ngoại lệ này và tự động format thành Error Response thống nhất trả về cho Front-End.

3. **Data Transfer Objects (DTO)**:
   - Tuyệt đối không dùng class trong `entity` để nhận request từ client, hoặc ném trực tiếp ra bên ngoài qua Controller.
   - Controller chỉ nhận RequestDTO và trả về ResponseDTO.
   - Tại RequestDTO, phải định nghĩa các validation constraint (`@NotNull`, `@NotBlank`, `@Size`, v.v.). Thêm `@Valid` ở tham số truyền vào Controller.

4. **Service Isolation (Cô lập Service)**:
   - Mỗi Module giữ DB/Table riêng. Nếu `OrderService` (Module Order) cần thông tin của `User` (Module User), nó PHẢI gọi qua `UserService`, CHỨ TUYỆT ĐỐI KHÔNG được gọi `UserRepository.findById()`.
