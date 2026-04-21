package com.unishare.api.common.constants;

/**
 * Trạng thái đơn apply mentor — cột {@code mentor_requests.status}.
 *
 * <p>State machine:
 * <pre>
 *   SUBMITTED ─► UNDER_REVIEW ─► APPROVED
 *                           └─► REJECTED
 *   REJECTED ─► SUBMITTED (resubmit)
 * </pre>
 */
public final class MentorRequestStatuses {

    private MentorRequestStatuses() {}

    public static final String SUBMITTED = "SUBMITTED";
    public static final String UNDER_REVIEW = "UNDER_REVIEW";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    public static boolean isTerminal(String status) {
        return APPROVED.equals(status);
    }

    public static boolean isReviewable(String status) {
        return SUBMITTED.equals(status) || UNDER_REVIEW.equals(status);
    }
}
