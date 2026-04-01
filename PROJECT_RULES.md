# Unishare API - Enterprise Spring Boot Guidelines & Rules

This products outlines the strict coding standards, architectural decisions, and best practices for the Unishare API. All developers and AI Agents MUST follow these rules to ensure the codebase remains maintainable, scalable, secure, and clean.

---

## 1. Architectural Pattern: Modular Monolith (Package-by-Feature)
The system is divided into business modules under `src/main/java/com/unishare/api/modules/`.
- **High Cohesion, Low Coupling**: Modules must be as independent as possible.
- **Strict Package Structure per Module**:
  - `controller/`: REST endpoints.
  - `service/`: Interfaces for business logic.
  - `service/impl/`: Implementations of the service interfaces.
  - `repository/`: Spring Data JPA interfaces.
  - `entity/`: Database schema mappings (JPA entities).
  - `dto/`: Request/Response payloads.
  - `mapper/`: MapStruct interfaces or manual mappers.

---

## 2. API Design & REST Conventions
- **Naming**: Use kebab-case for URLs, plural nouns for resources (e.g., `/api/v1/users`, `/api/v1/orders/{orderId}/items`).
- **Versioning**: Always prefix APIs with versioning (e.g., `/api/v1/`).
- **HTTP Methods**: 
  - `GET` for reading.
  - `POST` for creation (returns HTTP 201 Created).
  - `PUT` for full updates.
  - `PATCH` for partial updates.
  - `DELETE` for removal (returns HTTP 204 No Content).
- **Pagination**: Any endpoint returning a list of items MUST be paginated strings via Pageable (`?page=0&size=20&sort=createdAt,desc`).

---

## 3. SOLID & Clean Code Rules
- **Single Responsibility (SRP)**: Controllers parse/route; Services handle business rules; Repositories query the database.
- **Dependency Inversion (DIP)**: 
  - **Always rely on Interfaces** for Inter-Module communication. 
  - **Constructor Injection**: Use Lombok's `@RequiredArgsConstructor` with `private final` fields. **Never use `@Autowired` on fields.**
- **Service Isolation**: Module A MUST NEVER call Module B's `repository`. It must call Module B's `service`.
- **Immutability**: Prefer immutable DTOs and objects. Use `val` (if Kotlin) or generic `final` classes. Return unmodifiable lists where appropriate (`List.copyOf()`).
- **Optional vs Null**: Never return `null` from a service method fetching a single entity; return `Optional<T>` instead.

---

## 4. DTOs and Data Mapping
- **Entity encapsulation**: JPA Entities MUST NOT be exposed in REST controllers (Requests or Responses). They are strictly for database interaction.
- **Validation**:
  - Apply `jakarta.validation.constraints` (`@NotNull`, `@NotBlank`, `@Email`, `@Min`, `@Max`) on Request DTOs.
  - Trigger validation in Controllers using `@Valid` or `@Validated`.
- **Mapping**: Use MapStruct or static factory methods (`UserResponse.fromEntity(user)`) to convert Entity <-> DTO.

---

## 5. Database & Transaction Management
- **Transactions**: 
  - Apply `@Transactional` explicitly at the Service implementation layer.
  - Use `@Transactional(readOnly = true)` for methods that only fetch data. This optimizes Hibernate's dirty checking and routing.
- **N+1 Problem**: Always use `JOIN FETCH` in JPQL, `@EntityGraph`, or Hibernate Batch Fetching to avoid N+1 queries when fetching associated entities.
- **Soft Deletes**: Consider using a `deleted` boolean flag rather than hard-deleting records (`DELETE` from DB), using Hibernate `@SQLDelete` and `@Where`.
- **Auditing**: Entities should extend a `BaseEntity` containing `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, and `@LastModifiedBy`.

---

## 6. Exception Handling & Error Formatting
- **Global Handling**: Handled via a `@RestControllerAdvice` in the `infrastructure/` or `common/` package.
- **No Stack Traces**: Never expose Java stack traces to the client.
- **Custom Exceptions**: Use custom exceptions extending `RuntimeException` (e.g., `ResourceNotFoundException`, `UnauthorizedException`, `BusinessRuleViolationException`).
- **Standard Response**: The API must always return a predictable JSON error format:
  ```json
  {
    "timestamp": "2026-03-26T12:00:00Z",
    "status": 404,
    "error": "Not Found",
    "message": "User with ID 123 does not exist.",
    "path": "/api/v1/users/123"
  }
  ```

---

## 7. Security Rules
- **Stateless**: Use JWT (JSON Web Tokens) for authentication. No server-side session state (`SessionCreationPolicy.STATELESS`).
- **Authorization**: Secure endpoints using `@PreAuthorize("hasRole('ADMIN')")`.
- **Passwords**: Always hash passwords using `BCryptPasswordEncoder`. NEVER store plain text.
- **Data sanitization**: Prevent SQL Injection by strictly using JPA/Hibernate parameters. Prevent XSS by escaping outputs on the frontend and validating inputs on the backend.

---

## 8. Logging & Tracing
- **SLF4J**: Use `@Slf4j` (Lombok) for logging. Avoid `System.out.println()`.
- **Log Levels**: 
  - `ERROR`: System failures, unexpected exceptions.
  - `WARN`: Handled exceptions, suspicious activities (e.g., incorrect passwords).
  - `INFO`: Business events (e.g., "User logged in", "Order created").
  - `DEBUG`: Troubleshooting details (SQL queries, payload data).
- **MDC (Mapped Diagnostic Context)**: Inject a unique `traceId` or `correlationId` into the MDC for every incoming request so logs can easily trace requests across functions.

---

## 9. Testing Standards
- **Unit Tests**: 
  - Focus on Business Logic (Services). 
  - Use JUnit 5 + Mockito. 
  - Mock Repositories and external HTTP calls.
  - Naming convention: `methodName_stateUnderTest_expectedBehavior()` (e.g., `createUser_whenEmailExists_shouldThrowException()`).
- **Integration Tests**: 
  - Focus on Repositories (Data JPA Tests) and Controllers (MockMvc).
  - Testcontainers (Docker) should be used to spin up a real PostgreSQL database rather than an in-memory H2 database, ensuring accurate SQL behavior.
