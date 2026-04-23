package com.thaca.common.dtos.search;

import java.util.List;

public record SearchResponse<T>(List<T> data, PaginationResponse<?> pagination) {
    public static <T> SearchResponse<T> of(List<T> data, PaginationResponse<?> pagination) {
        return new SearchResponse<>(data, pagination);
    }

    public static <T> SearchResponse<T> empty() {
        return new SearchResponse<>(List.of(), PaginationResponse.empty());
    }

    public static <T> SearchResponse<T> of(List<T> data) {
        return new SearchResponse<>(data, PaginationResponse.empty());
    }
}
