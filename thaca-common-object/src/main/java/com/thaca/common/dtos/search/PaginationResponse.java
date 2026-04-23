package com.thaca.common.dtos.search;

import org.springframework.data.domain.Page;

public record PaginationResponse<T>(int currentPage, int totalPages, int size, int count, int total) {
    public static PaginationResponse<?> of(int currentPage, int totalPages, int size, int count, int total) {
        return new PaginationResponse<>(currentPage, totalPages, size, count, total);
    }

    public static <T> PaginationResponse<T> of(Page<T> page) {
        return new PaginationResponse<>(
            page.getNumber(),
            page.getTotalPages(),
            page.getSize(),
            page.getNumberOfElements(),
            (int) page.getTotalElements()
        );
    }

    public static PaginationResponse<?> empty() {
        return new PaginationResponse<>(0, 0, 0, 0, 0);
    }
}
