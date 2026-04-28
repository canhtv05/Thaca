package com.thaca.auth.services;

import com.thaca.auth.domains.Plan;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.mappers.PlanMapper;
import com.thaca.auth.repositories.PlanRepository;
import com.thaca.auth.validators.core.Validator;
import com.thaca.auth.validators.rules.CodeRule;
import com.thaca.common.constants.InternalMethod;
import com.thaca.common.dtos.internal.PlanDTO;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.common.enums.CommonStatus;
import com.thaca.common.enums.PlanType;
import com.thaca.common.excel.ExcelEngine;
import com.thaca.common.excel.schema.ExcelColumn;
import com.thaca.common.excel.schema.ExcelDataType;
import com.thaca.common.excel.schema.ExcelSchema;
import com.thaca.framework.blocking.starter.services.CommonService;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.dtos.ApiHeader;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.utils.CommonUtils;
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
public class PlanService {

    private final PlanRepository planRepository;

    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_PLANS, type = ModeType.VALIDATE)
    public void validateSearchPlans(SearchRequest<PlanDTO> request) {
        CommonService.validateSearchRequest(request);
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_PLANS, type = ModeType.HANDLE)
    public SearchResponse<PlanDTO> searchPlans(SearchRequest<PlanDTO> request) {
        Specification<Plan> spec = createPlanSpecification(request);
        Page<Plan> plans = planRepository.findAll(spec, request.getPage().toPageable(Sort.Direction.DESC, "updatedAt"));
        return new SearchResponse<>(
            plans.getContent().stream().map(PlanMapper::fromEntity).collect(Collectors.toList()),
            PaginationResponse.of(plans)
        );
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_PLAN, type = ModeType.VALIDATE)
    public void validateGetPlan(PlanDTO request) {
        if (CommonUtils.isEmpty(request.getCode())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_PLAN, type = ModeType.HANDLE)
    public PlanDTO getPlan(PlanDTO request) {
        return planRepository
            .findByCode(request.getCode())
            .map(PlanMapper::fromEntity)
            .orElseThrow(() -> new FwException(ErrorMessage.PLAN_NOT_FOUND));
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_CREATE_PLAN, type = ModeType.VALIDATE)
    public void validateCreatePlan(PlanDTO request) {
        validatePlan(request);
        if (planRepository.existsByCode(request.getCode())) {
            throw new FwException(CommonErrorMessage.CONFLICT);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_CREATE_PLAN, type = ModeType.HANDLE)
    public void createPlan(PlanDTO request) {
        Plan plan = new Plan();
        plan.setCode(request.getCode());
        plan.setName(request.getName());
        plan.setType(request.getType());
        plan.setMaxUsers(ObjectUtils.getIfNull(request.getMaxUsers(), 0));
        plan.setStatus(ObjectUtils.getIfNull(request.getStatus(), CommonStatus.ACTIVE));
        planRepository.save(plan);
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_UPDATE_PLAN, type = ModeType.VALIDATE)
    public void validateUpdatePlan(PlanDTO request) {
        validatePlan(request);
        if (CommonUtils.isEmpty(request.getCode())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_UPDATE_PLAN, type = ModeType.HANDLE)
    public void updatePlan(PlanDTO request) {
        Plan plan = planRepository
            .findByCode(request.getCode())
            .orElseThrow(() -> new FwException(ErrorMessage.PLAN_NOT_FOUND));
        if (CommonStatus.INACTIVE.equals(plan.getStatus())) {
            throw new FwException(ErrorMessage.PLAN_INACTIVE_CANNOT_UPDATE);
        }
        plan.setName(request.getName());
        plan.setType(request.getType());
        plan.setMaxUsers(ObjectUtils.getIfNull(request.getMaxUsers(), 0));
        planRepository.save(plan);
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_LOCK_UNLOCK_PLAN, type = ModeType.VALIDATE)
    public void validateLockUnlockPlan(PlanDTO request) {
        if (CommonUtils.isEmpty(request.getCode())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_LOCK_UNLOCK_PLAN, type = ModeType.HANDLE)
    public void lockUnlockPlan(PlanDTO request) {
        Plan plan = planRepository
            .findByCode(request.getCode())
            .orElseThrow(() -> new FwException(ErrorMessage.PLAN_NOT_FOUND));
        plan.setStatus(request.getStatus());
        planRepository.save(plan);
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_ALL_PLANS, type = ModeType.HANDLE)
    public List<PlanDTO> getAllPlans() {
        return planRepository
            .findAllActivePlansOrderByUpdatedAtDesc()
            .stream()
            .map(PlanMapper::fromEntity)
            .collect(Collectors.toList());
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_EXPORT_PLAN, type = ModeType.HANDLE)
    public byte[] exportPlan(SearchRequest<PlanDTO> request) throws IOException {
        Specification<Plan> spec = createPlanSpecification(request);
        List<PlanDTO> plans = planRepository
            .findAll(spec, Sort.by(Sort.Direction.DESC, "updatedAt"))
            .stream()
            .map(PlanMapper::fromEntity)
            .toList();
        List<Map<String, Object>> rows = new ArrayList<>();
        ApiHeader header = FwContextHeader.get();
        boolean isVietnamese = "vi".equals(header.getLanguage());
        for (PlanDTO plan : plans) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", plan.getCode());
            row.put("name", plan.getName());
            row.put("type", plan.getType().getValue());
            row.put("maxUsers", plan.getMaxUsers());
            row.put("status", plan.getStatus().getLabel(isVietnamese));
            row.put("createdAt", plan.getCreatedAt());
            row.put("createdBy", plan.getCreatedBy());
            row.put("updatedAt", plan.getUpdatedAt());
            row.put("updatedBy", plan.getUpdatedBy());
            rows.add(row);
        }
        return ExcelEngine.exportData(buildSchema(isVietnamese), rows);
    }

    private ExcelSchema buildSchema(boolean isVietnamese) {
        return ExcelSchema.builder()
            .sheetName(ObjectUtils.notEqual(isVietnamese, false) ? "Danh sách kế hoạch" : "Plans")
            .addColumn(
                ExcelColumn.builder("code", ObjectUtils.notEqual(isVietnamese, false) ? "Mã gói" : "Plan code")
                    .required()
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("name", ObjectUtils.notEqual(isVietnamese, false) ? "Tên gói" : "Plan name")
                    .required()
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("type", ObjectUtils.notEqual(isVietnamese, false) ? "Loại gói" : "Plan type")
                    .required()
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder(
                    "maxUsers",
                    ObjectUtils.notEqual(isVietnamese, false) ? "Users tối đa" : "Max Users"
                )
                    .required()
                    .dataType(ExcelDataType.NUMBER)
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

    private Specification<Plan> createPlanSpecification(SearchRequest<PlanDTO> request) {
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
                if (request.getFilter().getType() != null) {
                    p = cb.and(p, cb.equal(root.get("type"), request.getFilter().getType()));
                }
                if (request.getFilter().getStatus() != null) {
                    p = cb.and(p, cb.equal(root.get("status"), request.getFilter().getStatus()));
                }
            }
            return p;
        };
    }

    private void validatePlan(PlanDTO request) {
        if (CommonUtils.isEmpty(request.getCode(), request.getName(), request.getType())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        Validator<String> codeValidator = new Validator<>(List.of(new CodeRule()));
        codeValidator.validate(request.getCode());
        if (request.getType() == null) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (
            request.getType() != PlanType.FREE &&
            request.getType() != PlanType.BASIC &&
            request.getType() != PlanType.PRO &&
            request.getType() != PlanType.ENTERPRISE
        ) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
    }
}
