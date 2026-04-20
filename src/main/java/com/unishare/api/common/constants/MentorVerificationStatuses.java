package com.unishare.api.common.constants;

/** Trạng thái xác minh mentor (cột {@code mentor_profiles.verification_status}). */
public final class MentorVerificationStatuses {

    private MentorVerificationStatuses() {}

    public static final String PENDING = "pending";

    /** Đã xác minh — hiển thị trên danh bạ công khai. */
    public static final String VERIFIED = "verified";
}
