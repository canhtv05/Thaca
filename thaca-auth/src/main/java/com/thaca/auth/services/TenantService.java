package com.thaca.auth.services;

import com.thaca.auth.domains.Plan;
import com.thaca.auth.domains.Tenant;
import com.thaca.auth.domains.projections.PlanInfoProjection;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.mappers.PlanMapper;
import com.thaca.auth.mappers.TenantMapper;
import com.thaca.auth.repositories.PlanRepository;
import com.thaca.auth.repositories.TenantRepository;
import com.thaca.auth.validators.core.Validator;
import com.thaca.auth.validators.rules.CodeRule;
import com.thaca.auth.validators.rules.EmailRule;
import com.thaca.common.constants.InternalMethod;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.dtos.internal.projection.PlanInfoPrj;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.common.enums.CommonStatus;
import com.thaca.common.enums.TenantStatus;
import com.thaca.common.excel.ExcelEngine;
import com.thaca.common.excel.schema.ExcelColumn;
import com.thaca.common.excel.schema.ExcelDataType;
import com.thaca.common.excel.schema.ExcelSchema;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.dtos.ApiHeader;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.utils.CommonUtils;
import com.thaca.framework.core.utils.DateUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_TENANTS, type = ModeType.HANDLE)
    public SearchResponse<TenantDTO> searchTenants(SearchRequest<TenantDTO> request) {
        Specification<Tenant> spec = createTenantSpecification(request);
        Result result = getResult(request, spec);
        return new SearchResponse<>(
            result
                .tenants()
                .getContent()
                .stream()
                .map(tenant -> {
                    TenantDTO tenantDTO = TenantMapper.fromEntity(tenant);
                    PlanInfoProjection planInfo = result.planMap().getOrDefault(tenant.getPlan().getId(), null);
                    tenantDTO.setPlan(
                        planInfo != null
                            ? PlanInfoPrj.builder().id(planInfo.getId()).name(planInfo.getName()).build()
                            : null
                    );
                    return tenantDTO;
                })
                .collect(Collectors.toList()),
            PaginationResponse.of(result.tenants())
        );
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_CREATE_TENANT, type = ModeType.VALIDATE)
    public void validateCreateTenant(TenantDTO request) {
        validateTenant(request);
        if (tenantRepository.existsByCode(request.getCode())) {
            throw new FwException(CommonErrorMessage.CONFLICT);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_CREATE_TENANT, type = ModeType.HANDLE)
    public void createTenant(TenantDTO dto) {
        Plan plan = planRepository
            .findById(dto.getPlanId())
            .orElseThrow(() -> new FwException(ErrorMessage.PLAN_NOT_FOUND));
        if (CommonStatus.INACTIVE.equals(plan.getStatus())) {
            throw new FwException(ErrorMessage.PLAN_INACTIVE_CANNOT_SAVE);
        }
        Tenant tenant = Tenant.builder()
            .code(dto.getCode().toUpperCase(Locale.ROOT))
            .name(dto.getName())
            .domain(dto.getDomain())
            .status(dto.getStatus())
            .plan(plan)
            .expiresAt(DateUtils.stringToLocalDate(dto.getExpiresAt()))
            .contactEmail(dto.getContactEmail())
            .logoUrl(dto.getLogoUrl())
            .build();
        tenantRepository.save(tenant);
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_UPDATE_TENANT, type = ModeType.VALIDATE)
    public void validateUpdateTenant(TenantDTO request) {
        validateTenant(request);
        if (request.getId() == null) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_UPDATE_TENANT, type = ModeType.HANDLE)
    public void updateTenant(TenantDTO dto) {
        Tenant tenant = tenantRepository
            .findById(dto.getId())
            .orElseThrow(() -> new FwException(ErrorMessage.TENANT_NOT_FOUND));
        Plan plan = planRepository
            .findById(dto.getPlanId())
            .orElseThrow(() -> new FwException(ErrorMessage.PLAN_NOT_FOUND));
        if (CommonStatus.INACTIVE.equals(plan.getStatus())) {
            throw new FwException(ErrorMessage.PLAN_INACTIVE_CANNOT_SAVE);
        }

        tenant.setName(dto.getName());
        tenant.setDomain(dto.getDomain());
        tenant.setStatus(dto.getStatus());
        tenant.setPlan(plan);
        tenant.setExpiresAt(DateUtils.stringToLocalDate(dto.getExpiresAt()));
        tenant.setContactEmail(dto.getContactEmail());
        tenant.setLogoUrl(dto.getLogoUrl());
        tenant.setVersion(dto.getVersion());
        tenantRepository.save(tenant);
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_LOCK_UNLOCK_TENANT, type = ModeType.VALIDATE)
    public void validateLockUnlock(TenantDTO request) {
        if (request.getStatus() == null) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (
            !TenantStatus.ACTIVE.equals(request.getStatus()) &&
            !TenantStatus.INACTIVE.equals(request.getStatus()) &&
            !TenantStatus.SUSPENDED.equals(request.getStatus())
        ) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_LOCK_UNLOCK_TENANT, type = ModeType.HANDLE)
    public void lockUnlockTenant(TenantDTO dto) {
        Tenant tenant = tenantRepository
            .findById(dto.getId())
            .orElseThrow(() -> new FwException(ErrorMessage.TENANT_NOT_FOUND));
        tenant.setStatus(dto.getStatus());
        tenantRepository.save(tenant);
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_TENANT, type = ModeType.VALIDATE)
    public void validateGetTenant(TenantDTO request) {
        if (request.getCode() == null) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_TENANT, type = ModeType.HANDLE)
    public TenantDTO getTenant(TenantDTO dto) {
        Tenant tenant = tenantRepository
            .findByCode(dto.getCode())
            .orElseThrow(() -> new FwException(ErrorMessage.TENANT_NOT_FOUND));
        var result = TenantMapper.fromEntity(tenant);
        result.setPlanInfo(PlanMapper.fromEntity(tenant.getPlan()));
        return result;
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_EXPORT_TENANT, type = ModeType.HANDLE)
    public byte[] exportTenant(SearchRequest<TenantDTO> request) throws IOException {
        Specification<Tenant> spec = createTenantSpecification(request);
        Result result = getResult(request, spec);
        List<TenantDTO> tenants = result.tenants.stream().map(TenantMapper::fromEntity).toList();
        List<Map<String, Object>> rows = new ArrayList<>();
        ApiHeader header = FwContextHeader.get();
        boolean isVietnamese = "vi".equals(header.getLanguage());
        for (TenantDTO tenant : tenants) {
            PlanInfoProjection planInfo = result.planMap.get(tenant.getPlanId());
            String expiresAt = isVietnamese && StringUtils.isEmpty(tenant.getExpiresAt()) ? "Vô thời hạn" : "Infinity";
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", tenant.getCode());
            row.put("name", tenant.getName());
            row.put("domain", tenant.getDomain());
            row.put("contactEmail", tenant.getContactEmail());
            row.put("logoUrl", tenant.getLogoUrl());
            row.put("plan", planInfo != null ? planInfo.getName() : null);
            row.put("status", tenant.getStatus().getLabel(isVietnamese));
            row.put("version", tenant.getVersion());
            row.put("expiresAt", StringUtils.defaultIfBlank(tenant.getExpiresAt(), expiresAt));
            row.put("createdAt", tenant.getCreatedAt());
            row.put("createdBy", tenant.getCreatedBy());
            row.put("updatedAt", tenant.getUpdatedAt());
            row.put("updatedBy", tenant.getUpdatedBy());
            rows.add(row);
        }
        return ExcelEngine.exportData(buildSchema(isVietnamese), rows);
    }

    private ExcelSchema buildSchema(boolean isVietnamese) {
        return ExcelSchema.builder()
            .sheetName(ObjectUtils.notEqual(isVietnamese, false) ? "Danh sách tổ chức" : "Tenants")
            .addColumn(
                ExcelColumn.builder("code", ObjectUtils.notEqual(isVietnamese, false) ? "Mã Tenant" : "Tenant code")
                    .required()
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("name", ObjectUtils.notEqual(isVietnamese, false) ? "Tên Tenant" : "Tenant name")
                    .required()
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("domain", ObjectUtils.notEqual(isVietnamese, false) ? "Tên miền" : "Domain")
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder(
                    "contactEmail",
                    ObjectUtils.notEqual(isVietnamese, false) ? "Email liên hệ" : "Contact Email"
                )
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(ExcelColumn.builder("logoUrl", "Logo URL").dataType(ExcelDataType.STRING).build())
            .addColumn(
                ExcelColumn.builder("plan", ObjectUtils.notEqual(isVietnamese, false) ? "Gói dịch vụ (Plan)" : "Plan")
                    .required()
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder(
                    "expiresAt",
                    ObjectUtils.notEqual(isVietnamese, false) ? "Ngày hết hạn" : "Expiration Date"
                )
                    .dataType(ExcelDataType.DATE)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("status", ObjectUtils.notEqual(isVietnamese, false) ? "Trạng thái" : "Status")
                    .required()
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("version", ObjectUtils.notEqual(isVietnamese, false) ? "Phiên bản" : "Version")
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("createdAt", ObjectUtils.notEqual(isVietnamese, false) ? "Ngày tạo" : "Created At")
                    .dataType(ExcelDataType.DATE)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("createdBy", ObjectUtils.notEqual(isVietnamese, false) ? "Người tạo" : "Created By")
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder(
                    "updatedAt",
                    ObjectUtils.notEqual(isVietnamese, false) ? "Ngày cập nhật" : "Updated At"
                )
                    .dataType(ExcelDataType.DATE)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder(
                    "updatedBy",
                    ObjectUtils.notEqual(isVietnamese, false) ? "Người cập nhật" : "Updated By"
                )
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .build();
    }

    private Specification<Tenant> createTenantSpecification(SearchRequest<TenantDTO> request) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (request.getFilter() != null) {
                if (StringUtils.isNotBlank(request.getFilter().getName())) {
                    p = cb.and(
                        p,
                        cb.like(cb.lower(root.get("name")), "%" + request.getFilter().getName().toLowerCase() + "%")
                    );
                }
                if (StringUtils.isNotBlank(request.getFilter().getCode())) {
                    p = cb.and(
                        p,
                        cb.like(cb.lower(root.get("code")), "%" + request.getFilter().getCode().toLowerCase() + "%")
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

    private void validateTenant(TenantDTO request) {
        if (CommonUtils.isEmpty(request.getCode(), request.getName(), request.getPlanId())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        Validator<String> codeValidator = new Validator<>(List.of(new CodeRule()));
        codeValidator.validate(request.getCode());
        if (StringUtils.isNotBlank(request.getContactEmail())) {
            Validator<UserDTO> validator = new Validator<>(List.of(new EmailRule<>()));
            validator.validate(UserDTO.builder().email(request.getContactEmail()).build());
        }
    }

    private record Result(Page<Tenant> tenants, Map<Long, PlanInfoProjection> planMap) {}

    private Result getResult(SearchRequest<TenantDTO> request, Specification<Tenant> spec) {
        CompletableFuture<Page<Tenant>> tenantFuture = CompletableFuture.supplyAsync(
            () -> tenantRepository.findAll(spec, request.getPage().toPageable(Sort.Direction.DESC, "updatedAt")),
            executor
        );
        CompletableFuture<Map<Long, PlanInfoProjection>> planMapFuture = CompletableFuture.supplyAsync(
            () ->
                planRepository.findAllPlanInfo().stream().collect(Collectors.toMap(PlanInfoProjection::getId, p -> p)),
            executor
        );
        return CompletableFuture.allOf(tenantFuture, planMapFuture)
            .thenApply(v -> new Result(tenantFuture.join(), planMapFuture.join()))
            .join();
    }
}
