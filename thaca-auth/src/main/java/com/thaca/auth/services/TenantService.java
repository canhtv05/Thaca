package com.thaca.auth.services;

import com.thaca.auth.domains.Plan;
import com.thaca.auth.domains.Tenant;
import com.thaca.auth.mappers.TenantMapper;
import com.thaca.auth.repositories.TenantRepository;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
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
    @FwMode(name = "cms.searchTenants", type = ModeType.HANDLE)
    public SearchResponse<TenantDTO> searchTenants(SearchRequest<TenantDTO> request) {
        Specification<Tenant> spec = createTenantSpecification(request);
        Page<Tenant> tenants = tenantRepository.findAll(
            spec,
            request.getPage().toPageable(Sort.Direction.DESC, "updatedAt")
        );
        return new SearchResponse<>(
            tenants.getContent().stream().map(TenantMapper::fromEntity).collect(Collectors.toList()),
            PaginationResponse.of(tenants)
        );
    }

    @Transactional(readOnly = true)
    @FwMode(name = "cms.getTenant", type = ModeType.HANDLE)
    public TenantDTO getTenant(Long id) {
        return tenantRepository
            .findById(id)
            .map(TenantMapper::fromEntity)
            .orElseThrow(() -> new FwException(CommonErrorMessage.NOT_FOUND));
    }

    @Transactional
    @FwMode(name = "cms.saveTenant", type = ModeType.HANDLE)
    public TenantDTO saveTenant(TenantDTO dto) {
        Tenant tenant;
        if (dto.getId() != null) {
            tenant = tenantRepository
                .findById(dto.getId())
                .orElseThrow(() -> new FwException(CommonErrorMessage.NOT_FOUND));
            tenant.setName(dto.getName());
            tenant.setDomain(dto.getDomain());
            tenant.setStatus(dto.getStatus());
            if (dto.getPlanId() != null) {
                tenant.setPlan(Plan.builder().id(dto.getPlanId()).build());
            }
            tenant.setPlanType(dto.getPlanType());
            tenant.setExpiresAt(dto.getExpiresAt());
            tenant.setContactEmail(dto.getContactEmail());
            tenant.setLogoUrl(dto.getLogoUrl());
        } else {
            tenant = Tenant.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .domain(dto.getDomain())
                .status(dto.getStatus())
                .plan(dto.getPlanId() != null ? Plan.builder().id(dto.getPlanId()).build() : null)
                .planType(dto.getPlanType())
                .expiresAt(dto.getExpiresAt())
                .contactEmail(dto.getContactEmail())
                .logoUrl(dto.getLogoUrl())
                .build();
        }
        return TenantMapper.fromEntity(tenantRepository.save(tenant));
    }

    @Transactional
    @FwMode(name = "cms.deleteTenant", type = ModeType.HANDLE)
    public void deleteTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id).orElseThrow(() -> new FwException(CommonErrorMessage.NOT_FOUND));
        tenant.setDeletedAt(java.time.LocalDateTime.now());
        tenantRepository.save(tenant);
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
