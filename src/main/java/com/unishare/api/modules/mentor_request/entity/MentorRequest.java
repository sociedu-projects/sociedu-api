package com.unishare.api.modules.mentor_request.entity;

import com.unishare.api.common.constants.MentorRequestStatuses;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Đơn apply mentor của user. Một user chỉ có 1 đơn "đang mở" ở mọi thời điểm —
 * enforce ở {@code service} để không cần unique constraint (cho phép re-submit tạo đơn mới tuỳ policy).
 * Ở đây giữ 1 row duy nhất/user và tái sử dụng khi user nộp lại.
 */
@Entity
@Table(name = "mentor_requests", indexes = {
        @Index(name = "idx_mentor_requests_user", columnList = "user_id"),
        @Index(name = "idx_mentor_requests_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MentorRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 32)
    private String status = MentorRequestStatuses.SUBMITTED;

    @Column(length = 255)
    private String headline;

    @Column(columnDefinition = "TEXT")
    private String bio;

    /** Danh sách expertise (JSON array of strings). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private java.util.List<String> expertise;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "hourly_rate", precision = 12, scale = 2)
    private BigDecimal hourlyRate;

    /** File id (UUID) của CV upload qua file-service. Null nếu user nộp URL thay cho file. */
    @Column(name = "cv_file_id")
    private UUID cvFileId;

    /** URL link CV ngoài (Google Drive, Dropbox) nếu không upload file. */
    @Column(name = "cv_url", length = 1024)
    private String cvUrl;

    /** Danh sách link portfolio (JSON array). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "portfolio_urls", columnDefinition = "json")
    private java.util.List<String> portfolioUrls;

    /** Danh sách chứng chỉ (JSON array of {name, issuer, year, url?}). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private java.util.List<CertificateItem> certificates;

    /** Lý do admin reject (nếu có). */
    @Column(length = 1024)
    private String reason;

    /** Note nội bộ của admin. */
    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    /** Số lần nộp lại (tăng dần khi user resubmit sau reject). */
    @Column(name = "resubmit_count", nullable = false)
    private int resubmitCount = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** DTO lồng trong JSON column — {@code certificates}. */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class CertificateItem {
        private String name;
        private String issuer;
        private Integer year;
        private String url;
    }
}
