# Quy Chuẩn Lập Trình — Unishare API

> **Đây là bộ luật tối cao.** Mọi code được sinh ra PHẢI tuân thủ toàn bộ các quy tắc dưới đây, không có ngoại lệ.

---

## 0. Checklist bắt buộc trước khi viết code

Trước khi sinh bất kỳ dòng code nào, agent PHẢI:
1. Đọc `SYSTEM.md` tại root → hiểu toàn cảnh hệ thống
2. Đọc `MODULE_FLOW.md` của module đang làm → hiểu nghiệp vụ
3. Đọc các entity/DTO/service interface hiện có trong module → KHÔNG tái tạo thứ đã tồn tại
4. Xác định rõ: flow này thuộc ai gọi ai trong `SYSTEM.md §5`

---

## 1. Kiến trúc: Modular Monolith (Package-by-Feature)

```
modules/{module}/
  controller/        ← Chỉ route + validate DTO + gọi Service
  service/           ← Interface nghiệp vụ (Use Cases)
    impl/            ← Implementation: 100% business logic ở đây
  repository/        ← Spring Data JPA — chỉ query DB
  entity/            ← JPA entity — ánh xạ DB table
  dto/               ← Request/Response payloads — KHÔNG expose Entity
  mapper/            ← MapStruct hoặc static factory
  MODULE_FLOW.md     ← Mô tả nghiệp vụ module (HIGH-LEVEL, không có step-by-step)
```

**Quy tắc tuyệt đối về cô lập module:**
- Module A **KHÔNG ĐƯỢC** inject Repository của Module B → phải gọi `IServiceB`
- Module A **KHÔNG ĐƯỢC** import Entity của Module B → dùng DTO/ID primitive
- Giao tiếp cross-module chỉ qua Service Interface

---

## 2. Nguyên tắc SOLID trong thực tế

### S — Single Responsibility
```java
// ✅ ĐÚNG: Controller chỉ nhận request và route
@PostMapping
public ResponseEntity<OrderResponse> createOrder(
        @Valid @RequestBody CreateOrderRequest request,
        @AuthenticationPrincipal UserPrincipal principal) {
    OrderResponse response = orderService.createOrder(request, principal.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

// ❌ SAI: Controller chứa business logic
@PostMapping
public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
    BigDecimal price = packageRepo.findById(request.getPackageId()).get().getPrice();
    if (price.compareTo(BigDecimal.ZERO) < 0) { ... } // Logic này thuộc về Service
}
```

### O — Open/Closed
- Dùng Strategy Pattern cho các logic có thể mở rộng (ví dụ: nhiều payment provider).
- Enum + switch thay vì if-else chain khi xử lý trạng thái.

### L — Liskov Substitution
- Service Implementation phải thỏa contract của Interface — không throw exception không được khai báo trong Interface.

### I — Interface Segregation
- Mỗi Service Interface chỉ khai báo methods liên quan đến 1 nhóm use case.
- Không tạo `IGenericService<T>` chứa hết mọi thứ.

### D — Dependency Inversion
```java
// ✅ ĐÚNG: Inject interface
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {
    private final IServicePackageService packageService; // Interface của module khác
    private final OrderRepository orderRepository;
}

// ❌ SAI: Inject implementation hoặc repository của module khác
private final ServicePackageRepository packageRepo; // Phá vỡ cô lập module
```

---

## 3. Quy chuẩn Controller

```java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Quản lý đơn hàng")
public class OrderController {

    private final IOrderService orderService;

    /**
     * Tạo đơn hàng mới. Buyer phải là MENTEE đang active.
     */
    @PostMapping
    @PreAuthorize("hasRole('MENTEE')")
    @Operation(summary = "Tạo đơn hàng", description = "Mentee đặt mua gói dịch vụ của Mentor")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request, principal.getId()));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.getOrder(orderId, principal.getId()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.getMyOrders(principal.getId(), pageable));
    }
}
```

**Quy tắc Controller:**
- Không có `if/else` business logic
- Không inject Repository
- Luôn dùng `@Valid` trên Request DTO
- Luôn có `@Operation` cho Swagger
- Tất cả endpoint có dữ liệu dạng danh sách PHẢI dùng `Pageable`
- Trả về `Page<T>` thay vì `List<T>`

---

## 4. Quy chuẩn Service Interface

```java
/**
 * Quản lý vòng đời đơn hàng: Tạo, xác nhận thanh toán, hoàn tiền.
 */
public interface IOrderService {

    /**
     * Tạo đơn hàng mới cho Mentee. Giá được lấy từ ServicePackageVersion hiện hành
     * — KHÔNG tin giá từ request.
     *
     * @param request  thông tin đặt hàng
     * @param buyerId  ID của Mentee đang đăng nhập
     * @return thông tin đơn hàng vừa tạo
     * @throws ResourceNotFoundException nếu gói dịch vụ không tồn tại hoặc đã tắt
     */
    OrderResponse createOrder(CreateOrderRequest request, Long buyerId);

    /**
     * Lấy chi tiết đơn hàng. Kiểm tra quyền: chỉ buyer hoặc mentor trong đơn mới được xem.
     *
     * @param orderId   ID đơn hàng
     * @param requesterId ID người đang yêu cầu
     * @return chi tiết đơn hàng
     * @throws ResourceNotFoundException nếu không tìm thấy đơn
     * @throws UnauthorizedException nếu không có quyền xem
     */
    OrderResponse getOrder(Long orderId, Long requesterId);

    /**
     * Xử lý kết quả thanh toán từ Module Payment. Cập nhật trạng thái đơn hàng
     * và kích hoạt booking nếu thành công.
     *
     * @param orderId  ID đơn hàng
     * @param success  kết quả thanh toán
     */
    void handlePaymentResult(Long orderId, boolean success);

    Page<OrderResponse> getMyOrders(Long userId, Pageable pageable);
}
```

---

## 5. Quy chuẩn Service Implementation

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // Mặc định readOnly, override ở method ghi
@Slf4j
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final IServicePackageService packageService; // Cross-module via interface
    private final IBookingService bookingService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional  // Override readOnly cho method ghi
    public OrderResponse createOrder(CreateOrderRequest request, Long buyerId) {
        log.info("Tạo đơn hàng: buyerId={}, packageId={}", buyerId, request.getPackageId());

        // 1. Lấy giá hiện hành từ module Service (KHÔNG tin client)
        ServicePackageVersionDto version = packageService
                .getDefaultVersion(request.getPackageId())
                .orElseThrow(() -> new ResourceNotFoundException("Gói dịch vụ không tồn tại hoặc đã tắt"));

        // 2. Tạo đơn hàng với trạng thái pending_payment
        Order order = Order.builder()
                .buyerId(buyerId)
                .serviceId(request.getPackageId())
                .totalAmount(version.getPrice())
                .status(OrderStatus.PENDING_PAYMENT)
                .build();

        // 3. Lưu vào DB
        Order saved = orderRepository.save(order);
        log.info("Đơn hàng tạo thành công: orderId={}", saved.getId());

        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void handlePaymentResult(Long orderId, boolean success) {
        // 1. Tìm đơn hàng
        Order order = findOrderById(orderId);

        // 2. Cập nhật trạng thái theo kết quả
        if (success) {
            order.setStatus(OrderStatus.PAID);
            order.setPaidAt(Instant.now());
            orderRepository.save(order);

            // 3. Kích hoạt module Booking
            bookingService.initializeBooking(orderId);
            log.info("Đơn hàng {} thanh toán thành công, đã khởi tạo booking", orderId);
        } else {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            log.warn("Đơn hàng {} thanh toán thất bại", orderId);
        }
    }

    @Override
    public OrderResponse getOrder(Long orderId, Long requesterId) {
        Order order = findOrderById(orderId);

        // Kiểm tra quyền: chỉ buyer hoặc mentor trong gói dịch vụ
        boolean isBuyer = order.getBuyerId().equals(requesterId);
        // Mentor check cần cross-module — giữ đơn giản, kiểm tra qua packageService
        if (!isBuyer) {
            packageService.assertMentorOwnsPackage(order.getServiceId(), requesterId);
        }

        return orderMapper.toResponse(order);
    }

    @Override
    public Page<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        return orderRepository.findByBuyerId(userId, pageable)
                .map(orderMapper::toResponse);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Đơn hàng không tồn tại: " + orderId));
    }
}
```

**Quy tắc Service Implementation:**
- `@Transactional(readOnly = true)` ở class level — override `@Transactional` cho method ghi
- Dùng `Optional.orElseThrow()` — **KHÔNG** dùng `.get()` trực tiếp
- Numbered comment cho block logic dài: `// 1. ... // 2. ... // 3. ...`
- Log `INFO` khi tạo/cập nhật thành công, `WARN` khi xử lý lỗi nghiệp vụ

---

## 6. Quy chuẩn Entity & Repository

```java
@Entity
@Table(name = "orders")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@SQLDelete(sql = "UPDATE orders SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId; // ID, không phải @ManyToOne — tránh cross-module leak

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private Instant paidAt;

    @CreationTimestamp
    private Instant createdAt;
}
```

```java
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

    // Dùng @Query cho các truy vấn phức tạp, tránh N+1
    @Query("SELECT o FROM Order o WHERE o.buyerId = :buyerId AND o.status = :status")
    List<Order> findByBuyerIdAndStatus(@Param("buyerId") Long buyerId,
                                        @Param("status") OrderStatus status);
}
```

**Quy tắc Entity:**
- Cross-module relation: Lưu `Long foreignId` — **KHÔNG** dùng `@ManyToOne` sang entity của module khác
- Soft delete bằng `deleted_at` + `@SQLDelete` + `@Where` — không dùng `boolean deleted`
- Enum lưu dưới dạng `STRING` — không dùng `ORDINAL`

---

## 7. Quy chuẩn DTO & Mapping

```java
// Request DTO — validation đầy đủ
public record CreateOrderRequest(
    @NotNull(message = "Vui lòng chọn gói dịch vụ")
    Long packageId
) {}

// Response DTO — static factory method
public record OrderResponse(Long id, Long buyerId, BigDecimal totalAmount, String status, Instant paidAt) {

    public static OrderResponse fromEntity(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getBuyerId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getPaidAt()
        );
    }
}
```

- Ưu tiên `record` cho DTO (immutable by default)
- Request DTO: **bắt buộc** có Jakarta Validation constraints với message tiếng Việt
- Response DTO: dùng `static fromEntity()` hoặc MapStruct Interface

---

## 8. Exception Handling

```java
// Throw custom exception trong Service — KHÔNG tự catch để trả response
throw new ResourceNotFoundException("Gói dịch vụ không tồn tại: " + packageId);
throw new UnauthorizedException("Bạn không có quyền xem đơn hàng này");
throw new BusinessRuleViolationException("Không thể hủy đơn hàng đã thanh toán");

// Global handler — đặt trong common/ package
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, ex.getMessage()));
    }
}
```

---

## 9. Quy chuẩn Tài liệu (Documentation)

| Vị trí | Nội dung bắt buộc |
|---|---|
| `SYSTEM.md` (root) | Toàn cảnh hệ thống, actor, module map, luồng xuyên module |
| `MODULE_FLOW.md` (module root) | Entities, core flows HIGH-LEVEL, quan hệ module, business rules |
| Controller class | `@Tag` Swagger, `@Operation` trên mỗi endpoint |
| Service Interface | Javadoc tiếng Việt: mục đích + `@param` + `@return` + `@throws` |
| Service Impl (method dài) | Comment số thứ tự: `// 1. ...`, `// 2. ...` |

**Rule cho MODULE_FLOW.md:**
- Tối đa 50 dòng
- KHÔNG mô tả chi tiết từng API (thuộc Javadoc/Swagger)
- KHÔNG viết step-by-step implementation (thuộc code comment)
- CHỈ: entities, flow HIGH-LEVEL (trigger → action → result), dependencies, business rules

---

## 10. Security Rules

```java
// Kiểm tra ownership trong Service — KHÔNG để Controller tự check
public OrderResponse getOrder(Long orderId, Long requesterId) {
    Order order = findOrderById(orderId);
    if (!order.getBuyerId().equals(requesterId)) {
        throw new UnauthorizedException("Bạn không có quyền xem đơn hàng này");
    }
    return orderMapper.toResponse(order);
}

// Endpoint annotation
@PreAuthorize("hasRole('MENTOR')")    // Chỉ Mentor
@PreAuthorize("hasRole('ADMIN')")     // Chỉ Admin
@PreAuthorize("isAuthenticated()")    // Mọi user đã đăng nhập
// Public endpoint — không cần annotation, khai báo trong SecurityConfig
```

---

## 11. Testing Standards

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private IServicePackageService packageService;
    @InjectMocks private OrderServiceImpl orderService;

    @Test
    void createOrder_whenPackageNotFound_shouldThrowResourceNotFoundException() {
        // Given
        when(packageService.getDefaultVersion(anyLong())).thenReturn(Optional.empty());
        var request = new CreateOrderRequest(99L);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.createOrder(request, 1L));
    }

    @Test
    void createOrder_whenValid_shouldReturnOrderResponse() {
        // Given
        var version = new ServicePackageVersionDto(1L, new BigDecimal("500000"));
        when(packageService.getDefaultVersion(1L)).thenReturn(Optional.of(version));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0); o.setId(1L); return o;
        });

        // When
        OrderResponse result = orderService.createOrder(new CreateOrderRequest(1L), 10L);

        // Then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.totalAmount()).isEqualByComparingTo("500000");
        verify(orderRepository).save(any(Order.class));
    }
}
```
