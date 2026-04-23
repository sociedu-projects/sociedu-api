package com.unishare.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Envelope phân trang chuẩn cho toàn hệ thống – tránh lộ cấu trúc
 * {@link org.springframework.data.domain.Page} Spring ra client và giữ hợp đồng ổn định cho FE.
 *
 * @param <T> kiểu item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> items;
    private int page;
    private int size;
    private long total;
    private int totalPages;
    private String sort;

    /** Map từ {@link Page} sang {@link PageResponse} giữ nguyên danh sách items. */
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .items(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .sort(page.getSort().isSorted() ? page.getSort().toString() : null)
                .build();
    }

    /** Map từ {@link Page} + mapper element sang {@link PageResponse}. */
    public static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> mapper) {
        return PageResponse.<T>builder()
                .items(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .sort(page.getSort().isSorted() ? page.getSort().toString() : null)
                .build();
    }
}
