package com.unishare.api.modules.mentor_request.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** Body tùy chọn khi admin approve đơn mentor. */
@Data
public class ApproveMentorRequestRequest {

    @Size(max = 2000)
    private String note;
}
