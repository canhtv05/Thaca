package com.thaca.common.dtos.search;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public record PaginationRequest(
        Integer page,
        Integer size,
        String sortField,
        String sortOrder
) {

    private int safePage() {
        return (page == null || page < 1) ? 0 : page - 1;
    }

    private int safeSize() {
        return (size == null || size < 1) ? 20 : size;
    }

    public Pageable toPageable() {
        if (StringUtils.hasText(sortField) && StringUtils.hasText(sortOrder)) {
            return PageRequest.of(
                    safePage(),
                    safeSize(),
                    Sort.Direction.fromString(sortOrder),
                    sortField
            );
        }
        return PageRequest.of(safePage(), safeSize());
    }

    public Pageable toPageable(Sort.Direction defaultSort, String defaultField) {

        if (StringUtils.hasText(sortField) && StringUtils.hasText(sortOrder)) {
            return PageRequest.of(
                    safePage(),
                    safeSize(),
                    Sort.Direction.fromString(sortOrder),
                    sortField
            );
        }
        if (defaultSort != null && StringUtils.hasText(defaultField)) {
            return PageRequest.of(safePage(), safeSize(), defaultSort, defaultField);
        }
        return PageRequest.of(safePage(), safeSize());
    }
}
