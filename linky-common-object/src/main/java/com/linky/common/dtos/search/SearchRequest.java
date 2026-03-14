package com.linky.common.dtos.search;

public record SearchRequest<T>(T filter, PaginationRequest page) {
}