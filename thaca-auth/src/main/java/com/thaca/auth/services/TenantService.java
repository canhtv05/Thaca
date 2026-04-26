package com.thaca.auth.services;

import com.thaca.auth.domains.Tenant;
import com.thaca.auth.mappers.TenantMapper;
import com.thaca.auth.repositories.TenantRepository;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import jakarta.persistence.criteria.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public SearchResponse<TenantDTO> searchTenants(SearchRequest<TenantDTO> request) {
        Specification<Tenant> spec = createTenantSpecification(request);
        Page<Tenant> tenants = tenantRepository.findAll(
            spec,
            request.getPage().toPageable(Sort.Direction.DESC, "createdAt")
        );
        return new SearchResponse<>(
            tenants.getContent().stream().map(TenantMapper::fromEntity).collect(Collectors.toList()),
            PaginationResponse.of(tenants)
        );
    }

    private Specification<Tenant> createTenantSpecification(SearchRequest<TenantDTO> request) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (request.getFilter() != null) {
                if (StringUtils.isNotBlank(request.getFilter().getCode())) {
                    p = cb.and(
                        p,
                        cb.like(cb.lower(root.get("code")), "%" + request.getFilter().getCode().toLowerCase() + "%")
                    );
                }
                if (StringUtils.isNotBlank(request.getFilter().getName())) {
                    p = cb.and(
                        p,
                        cb.like(cb.lower(root.get("name")), "%" + request.getFilter().getName().toLowerCase() + "%")
                    );
                }
                if (request.getFilter().getStatus() != null) {
                    p = cb.and(p, cb.equal(root.get("status"), request.getFilter().getStatus()));
                }
            }
            return p;
        };
    }
}
