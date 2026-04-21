package com.unishare.api.modules.mentor_request.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Body khi admin reject đơn mentor — {@code reason} bắt buộc để user hiểu lý do. */
@Data
public class RejectMentorRequestRequest {

    @NotBlank(message = "Lý do từ chối không được trống")
    @Size(max = 1024)
    private String reason;

    @Size(max = 2000)
    private String note;
}
