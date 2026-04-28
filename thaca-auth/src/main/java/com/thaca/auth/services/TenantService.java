package com.thaca.auth.services;

import com.thaca.auth.domains.Plan;
import com.thaca.auth.domains.Tenant;
import com.thaca.auth.domains.projections.PlanInfoProjection;
import com.thaca.auth.enums.ErrorMessage;
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
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_TENANT, type = ModeType.VALIDATE)
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
            .code(dto.getCode())
            .name(dto.getName())
            .domain(dto.getDomain())
            .status(dto.getStatus())
            .plan(plan)
            .expiresAt(dto.getExpiresAt())
            .contactEmail(dto.getContactEmail())
            .logoUrl(dto.getLogoUrl())
            .build();
        tenantRepository.save(tenant);
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_TENANT, type = ModeType.VALIDATE)
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
        tenant.setExpiresAt(dto.getExpiresAt());
        tenant.setContactEmail(dto.getContactEmail());
        tenant.setLogoUrl(dto.getLogoUrl());
        tenantRepository.save(tenant);
    }

    @Transactional
    @FwMode(name = InternalMethod.INTERNAL_CMS_LOCK_UNLOCK_TENANT, type = ModeType.HANDLE)
    public void lockUnlockTenant(TenantDTO dto) {
        Tenant tenant = tenantRepository
            .findById(dto.getId())
            .orElseThrow(() -> new FwException(ErrorMessage.TENANT_NOT_FOUND));
        tenant.setStatus(dto.getStatus());
        tenantRepository.save(tenant);
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_EXPORT_TENANT, type = ModeType.HANDLE)
    public byte[] exportTenant(SearchRequest<TenantDTO> request) throws IOException {
        Specification<Tenant> spec = createTenantSpecification(request);
        List<TenantDTO> tenants = tenantRepository
            .findAll(spec, Sort.by(Sort.Direction.DESC, "updatedAt"))
            .stream()
            .map(TenantMapper::fromEntity)
            .collect(Collectors.toList());
        List<Map<String, Object>> rows = new ArrayList<>();
        ApiHeader header = FwContextHeader.get();
        boolean isVietnamese = "vi".equals(header.getLanguage());
        for (TenantDTO tenant : tenants) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", tenant.getCode());
            row.put("name", tenant.getName());
            row.put("domain", tenant.getDomain());
            row.put("contactEmail", tenant.getContactEmail());
            row.put("logoUrl", tenant.getLogoUrl());
            row.put("plan", tenant.getPlan() != null ? tenant.getPlan().getName() : null);
            row.put("expiresAt", tenant.getExpiresAt());
            row.put("status", tenant.getStatus().getLabel(isVietnamese));
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
            .addColumn(
                ExcelColumn.builder("logoUrl", ObjectUtils.notEqual(isVietnamese, false) ? "Logo URL" : "Logo URL")
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
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
                    .required()
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

    private void validateTenant(TenantDTO request) {
        if (CommonUtils.isEmpty(request.getCode(), request.getName(), request.getPlanId())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        Validator<String> codeValidator = new Validator<>(List.of(new CodeRule()));
        codeValidator.validate(request.getCode());
        if (StringUtils.isNotBlank(request.getContactEmail())) {
            Validator<UserDTO> validator = new Validator<>(List.of(new EmailRule<UserDTO>()));
            validator.validate(UserDTO.builder().email(request.getContactEmail()).build());
        }
    }
}
