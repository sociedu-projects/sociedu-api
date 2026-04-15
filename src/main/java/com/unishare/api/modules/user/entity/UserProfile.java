package com.unishare.api.modules.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(length = 150)
    private String headline;

    @Column(name = "avatar_file_id")
    private UUID avatarFileId;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String location;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** Tên hiển thị (auth / báo cáo): ghép first + last, mặc định khi trống. */
    public String getDisplayName() {
        String a = firstName != null ? firstName.trim() : "";
        String b = lastName != null ? lastName.trim() : "";
        String s = (a + " " + b).trim();
        return s.isEmpty() ? "Người dùng" : s;
    }
}
