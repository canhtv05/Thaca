package com.thaca.common.dtos.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {

    private Integer page;
    private Integer size;
    private String sortField;
    private String sortOrder;

    private int safePage() {
        return (page == null || page < 0) ? 0 : page;
    }

    private int safeSize() {
        return (size == null || size < 1) ? 20 : size;
    }

    public Pageable toPageable() {
        return toPageable(null, null);
    }

    public Pageable toPageable(Sort.Direction defaultSort, String defaultField) {
        int p = safePage();
        int s = safeSize();

        if (StringUtils.hasText(sortField)) {
            Sort.Direction direction = Sort.Direction.ASC;
            if (StringUtils.hasText(sortOrder)) {
                try {
                    direction = Sort.Direction.fromString(sortOrder.toUpperCase());
                } catch (Exception ignored) {}
            }
            return PageRequest.of(p, s, direction, sortField);
        }

        if (defaultSort != null && StringUtils.hasText(defaultField)) {
            return PageRequest.of(p, s, defaultSort, defaultField);
        }

        return PageRequest.of(p, s);
    }
}
