package com.unishare.api.modules.service.dto;

import java.util.UUID;

/** Dữ liệu tối thiểu để booking tạo session từ catalog — không expose entity ngoài module service. */
public record PackageCurriculumSeedItem(UUID id, String title) {
}
