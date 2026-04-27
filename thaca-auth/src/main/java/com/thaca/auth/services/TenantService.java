package com.thaca.auth.services;

import com.thaca.auth.domains.Plan;
import com.thaca.auth.domains.Tenant;
import com.thaca.auth.domains.projections.PlanInfoProjection;
import com.thaca.auth.mappers.TenantMapper;
import com.thaca.auth.repositories.PlanRepository;
import com.thaca.auth.repositories.TenantRepository;
import com.thaca.common.constants.InternalMethod;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.projection.PlanInfoPrj;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.Map;
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
    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_TENANTS, type = ModeType.HANDLE)
    public SearchResponse<TenantDTO> searchTenants(SearchRequest<TenantDTO> request) {
        Specification<Tenant> spec = createTenantSpecification(request);
        Page<Tenant> tenants = tenantRepository.findAll(
            spec,
            request.getPage().toPageable(Sort.Direction.DESC, "updatedAt")
        );
        Map<Long, PlanInfoProjection> planMap = planRepository
            .findAllPlanInfo()
            .stream()
            .collect(Collectors.toMap(PlanInfoProjection::getId, p -> p));
        return new SearchResponse<>(
            tenants
                .getContent()
                .stream()
                .map(tenant -> {
                    TenantDTO tenantDTO = TenantMapper.fromEntity(tenant);
                    PlanInfoProjection planInfo = planMap.getOrDefault(tenant.getPlan().getId(), null);
                    tenantDTO.setPlan(
                        planInfo != null
                            ? PlanInfoPrj.builder().id(planInfo.getId()).name(planInfo.getName()).build()
                            : null
                    );
                    return tenantDTO;
                })
                .collect(Collectors.toList()),
            PaginationResponse.of(tenants)
        );
    }

    @Transactional
    @FwMode(name = InternalMethod.INTERNAL_CMS_CREATE_TENANT, type = ModeType.HANDLE)
    public TenantDTO createTenant(TenantDTO dto) {
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
                .expiresAt(dto.getExpiresAt())
                .contactEmail(dto.getContactEmail())
                .logoUrl(dto.getLogoUrl())
                .build();
        }
        return TenantMapper.fromEntity(tenantRepository.save(tenant));
    }

    @Transactional
    @FwMode(name = InternalMethod.INTERNAL_CMS_LOCK_UNLOCK_TENANT, type = ModeType.HANDLE)
    public void lockUnlockTenant(TenantDTO dto) {
        Tenant tenant = tenantRepository
            .findById(dto.getId())
            .orElseThrow(() -> new FwException(CommonErrorMessage.NOT_FOUND));
        tenant.setStatus(dto.getStatus());
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
                if (request.getFilter().getPlanId() != null) {
                    Join<Tenant, Plan> join = root.join("plan", JoinType.LEFT);
                    p = cb.and(p, cb.equal(join.get("id"), request.getFilter().getPlanId()));
                }
            }
            return p;
        };
    }
}
