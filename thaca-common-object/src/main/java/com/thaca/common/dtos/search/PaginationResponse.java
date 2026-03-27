package com.thaca.common.dtos.search;

public record PaginationResponse(int currentPage, int totalPages, int size, int count, int total) {
    public static PaginationResponse of(int currentPage, int totalPages, int size, int count, int total) {
        return new PaginationResponse(currentPage, totalPages, size, count, total);
    }

    public static PaginationResponse empty() {
        return new PaginationResponse(0, 0, 0, 0, 0);
    }
}
