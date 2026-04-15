# Kỹ Thuật Nâng Cao — Unishare API

> Tập hợp các kỹ thuật Spring Boot chuyên sâu, pattern phổ biến và cách xử lý các tình huống đặc thù trong dự án.

---

## 1. Pattern: State Machine cho Đơn hàng / Booking

**Vấn đề**: Trạng thái entity cần chuyển theo quy tắc cứng. Không được nhảy trạng thái tuỳ tiện.

```java
// Enum với cho phép chuyển trạng thái hợp lệ
public enum OrderStatus {
    PENDING_PAYMENT {
        @Override public Set<OrderStatus> validTransitions() {
            return Set.of(PAID, FAILED, CANCELED);
        }
    },
    PAID {
        @Override public Set<OrderStatus> validTransitions() {
            return Set.of(REFUNDED);
        }
    },
    FAILED, CANCELED, REFUNDED {
        @Override public Set<OrderStatus> validTransitions() { return Set.of(); }
    };

    public abstract Set<OrderStatus> validTransitions();

    public void assertCanTransitionTo(OrderStatus next) {
        if (!validTransitions().contains(next)) {
            throw new BusinessRuleViolationException(
                "Không thể chuyển trạng thái từ " + this + " sang " + next);
        }
    }
}

// Dùng trong Service
order.getStatus().assertCanTransitionTo(OrderStatus.PAID);
order.setStatus(OrderStatus.PAID);
```

---

## 2. Pattern: Cross-Module Communication qua Service Interface

**Vấn đề**: Module Order cần biết giá từ Module Service, nhưng không được inject ServicePackageRepository.

```java
// Trong module 'service' — package service/
public interface IServicePackageService {
    /**
     * Lấy phiên bản giá mặc định của gói dịch vụ.
     * Trả về empty nếu gói không tồn tại hoặc is_active=false.
     */
    Optional<ServicePackageVersionDto> getDefaultVersion(Long packageId);

    void assertMentorOwnsPackage(Long packageId, Long mentorId);
}

// Trong module 'service' — DTO dùng để truyền cross-module
public record ServicePackageVersionDto(Long versionId, BigDecimal price, Integer duration) {}

// Trong module 'order' — inject IServicePackageService, NOT ServicePackageRepository
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {
    private final IServicePackageService packageService; // ✅ Interface từ module khác
}
```

---

## 3. Pattern: Security Context — Lấy User hiện tại

```java
// UserPrincipal — custom implementation của UserDetails
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    private final Long id;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    // ... các method UserDetails khác
}

// Cách lấy trong Controller
@GetMapping("/me")
public ResponseEntity<ProfileResponse> getMyProfile(
        @AuthenticationPrincipal UserPrincipal principal) {
    return ResponseEntity.ok(userService.getProfile(principal.getId()));
}

// Cách lấy trong Service (ít dùng, hạn chế coupling)
public Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return ((UserPrincipal) auth.getPrincipal()).getId();
}
```

---

## 4. Tránh N+1 Query — JOIN FETCH & @EntityGraph

```java
// ❌ SAI: Vòng lặp sinh N+1 query
List<Booking> bookings = bookingRepository.findAll();
bookings.forEach(b -> b.getSessions().size()); // Mỗi booking = 1 query SELECT sessions

// ✅ ĐÚNG: JOIN FETCH trong JPQL
@Query("SELECT b FROM Booking b LEFT JOIN FETCH b.sessions WHERE b.buyerId = :buyerId")
List<Booking> findByBuyerIdWithSessions(@Param("buyerId") Long buyerId);

// ✅ ĐÚNG: @EntityGraph trong Repository
@EntityGraph(attributePaths = {"sessions", "sessions.evidences"})
Optional<Booking> findWithSessionsById(Long id);
```

---

## 5. Soft Delete — Xóa mềm chuẩn

```java
@Entity
@SQLDelete(sql = "UPDATE files SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class FileEntity {
    private Instant deletedAt;
}

// Khi cần truy vấn kể cả đã xóa (ví dụ: audit)
@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    // Query bình thường: @Where tự lọc deleted_at IS NULL
    Optional<FileEntity> findById(Long id);

    // Muốn lấy cả đã xóa — dùng native query bypass @Where
    @Query(value = "SELECT * FROM files WHERE id = :id", nativeQuery = true)
    Optional<FileEntity> findByIdIncludingDeleted(@Param("id") Long id);
}
```

---

## 6. Xử lý Concurrency — Optimistic Locking

**Áp dụng khi**: Nhiều request có thể cùng lúc thay đổi 1 record (ví dụ: Payment Webhook).

```java
@Entity
public class PaymentTransaction {
    @Version
    private Long version; // Hibernate tự quản lý, tự tăng mỗi lần update

    // Nếu 2 request cùng update: request thứ 2 ném OptimisticLockException
}

// Handler trong GlobalExceptionHandler
@ExceptionHandler(OptimisticLockingFailureException.class)
public ResponseEntity<ErrorResponse> handleConcurrency(OptimisticLockingFailureException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse.of(409, "Dữ liệu đã được cập nhật bởi tiến trình khác, vui lòng thử lại"));
}
```

---

## 7. Idempotency — Chống xử lý trùng lặp Webhook

```java
@Transactional
public void processPaymentWebhook(PaymentWebhookRequest request) {
    // 1. Kiểm tra đã xử lý chưa bằng provider_transaction_id
    if (transactionRepository.existsByProviderTransactionId(request.getTransactionId())) {
        log.warn("Webhook đã được xử lý trước đó: {}", request.getTransactionId());
        return; // Bỏ qua, trả về 200 OK để Provider không retry
    }

    // 2. Verify chữ ký
    webhookVerifier.verify(request); // Throws nếu chữ ký sai

    // 3. Xử lý logic
    PaymentTransaction tx = buildTransaction(request);
    transactionRepository.save(tx);
    orderService.handlePaymentResult(tx.getOrderId(), request.isSuccess());
}
```

---

## 8. Phân trang & Sắp xếp chuẩn

```java
// Repository — luôn nhận Pageable
Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

// Controller — nhận Pageable từ query params (?page=0&size=20&sort=createdAt,desc)
@GetMapping
public ResponseEntity<Page<OrderResponse>> getMyOrders(
        @AuthenticationPrincipal UserPrincipal principal,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable) {
    return ResponseEntity.ok(orderService.getMyOrders(principal.getId(), pageable));
}
```

---

## 9. Logging chuẩn với SLF4J + MDC

```java
@Slf4j
@Service
public class OrderServiceImpl implements IOrderService {

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Long buyerId) {
        // Log cấp INFO cho business event quan trọng
        log.info("Khởi tạo đơn hàng: buyerId={}, packageId={}", buyerId, request.getPackageId());

        // ... logic ...

        log.info("Đơn hàng tạo thành công: orderId={}, amount={}", saved.getId(), saved.getTotalAmount());
        return orderMapper.toResponse(saved);
    }
}

// Filter để inject traceId vào MDC cho mọi request
@Component
public class MdcLoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws IOException, ServletException {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("traceId", traceId);
        response.setHeader("X-Trace-Id", traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
// Thêm %X{traceId} vào pattern trong logback.xml
```

---

## 10. Pattern: Builder cho Entity phức tạp

```java
// Dùng @Builder + static factory thay vì constructor dài
Order order = Order.builder()
        .buyerId(buyerId)
        .serviceId(request.getPackageId())
        .totalAmount(version.getPrice())
        .status(OrderStatus.PENDING_PAYMENT)
        .build();

// Với entity con bắt buộc liên kết cha
BookingSession session = BookingSession.builder()
        .bookingId(booking.getId())  // Long ID, không phải @ManyToOne reference
        .curriculumId(curriculum.getId())
        .title(curriculum.getTitle())
        .status(SessionStatus.PENDING)
        .build();
```

---

## 11. Validation tùy chỉnh (Custom Constraint)

```java
// Khi Jakarta annotation không đủ — ví dụ: kiểm tra ngày hợp lệ
@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message() default "Ngày bắt đầu phải trước ngày kết thúc";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, UserEducationRequest> {
    @Override
    public boolean isValid(UserEducationRequest req, ConstraintValidatorContext ctx) {
        if (req.getStartDate() == null || req.getEndDate() == null) return true;
        return req.getStartDate().isBefore(req.getEndDate());
    }
}

// Áp dụng lên DTO class level
@ValidDateRange
public record UserEducationRequest(LocalDate startDate, LocalDate endDate, String degree) {}
```

---

## 12. Cấu trúc Error Response chuẩn

```java
// ErrorResponse nhất quán cho mọi lỗi
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(Instant.now(), status,
                HttpStatus.valueOf(status).getReasonPhrase(), message, null);
    }
}

// JSON output:
// {
//   "timestamp": "2026-04-13T00:00:00Z",
//   "status": 404,
//   "error": "Not Found",
//   "message": "Gói dịch vụ không tồn tại: 99",
//   "path": "/api/v1/orders"
// }
```

---

## 13. Agent sinh code — Checklist tự kiểm tra

Trước khi hoàn thành task, agent phải tự verify:

- [ ] Entity KHÔNG dùng `@ManyToOne` sang entity của module khác — dùng `Long foreignId`
- [ ] Service Implementation KHÔNG inject Repository của module khác
- [ ] Controller KHÔNG có `if/else` business logic
- [ ] Mọi List endpoint đều dùng `Page<T>` và nhận `Pageable`
- [ ] Method ghi có `@Transactional`, method đọc có `@Transactional(readOnly = true)`
- [ ] Không dùng `Optional.get()` — luôn dùng `orElseThrow()`
- [ ] Request DTO có đủ Jakarta validation annotation
- [ ] Tất cả public method trong Service Interface có Javadoc tiếng Việt
- [ ] Không expose Entity trong Controller response — luôn dùng Response DTO
- [ ] Enum được lưu dưới dạng `@Enumerated(EnumType.STRING)`, không phải ORDINAL
