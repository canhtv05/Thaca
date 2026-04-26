package com.thaca.auth.services;

import com.thaca.auth.domains.Plan;
import com.thaca.auth.mappers.PlanMapper;
import com.thaca.auth.repositories.PlanRepository;
import com.thaca.common.dtos.internal.PlanDTO;
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
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    @FwMode(name = "cms.searchPlans", type = ModeType.HANDLE)
    public SearchResponse<PlanDTO> searchPlans(SearchRequest<PlanDTO> request) {
        Specification<Plan> spec = createPlanSpecification(request);
        Page<Plan> plans = planRepository.findAll(spec, request.getPage().toPageable(Sort.Direction.DESC, "createdAt"));
        return new SearchResponse<>(
            plans.getContent().stream().map(PlanMapper::fromEntity).collect(Collectors.toList()),
            PaginationResponse.of(plans)
        );
    }

    @Transactional(readOnly = true)
    @FwMode(name = "cms.getPlan", type = ModeType.HANDLE)
    public PlanDTO getPlan(Long id) {
        return planRepository
            .findById(id)
            .map(PlanMapper::fromEntity)
            .orElseThrow(() -> new FwException(CommonErrorMessage.NOT_FOUND));
    }

    @Transactional
    @FwMode(name = "cms.savePlan", type = ModeType.HANDLE)
    public PlanDTO savePlan(PlanDTO dto) {
        Plan plan;
        if (dto.getId() != null) {
            plan = planRepository
                .findById(dto.getId())
                .orElseThrow(() -> new FwException(CommonErrorMessage.NOT_FOUND));
            plan.setName(dto.getName());
            plan.setType(dto.getType());
            plan.setMaxUsers(dto.getMaxUsers());
            plan.setStatus(dto.getStatus());
        } else {
            plan = Plan.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .type(dto.getType())
                .maxUsers(dto.getMaxUsers())
                .status(dto.getStatus())
                .build();
        }
        return PlanMapper.fromEntity(planRepository.save(plan));
    }

    @Transactional
    @FwMode(name = "cms.deletePlan", type = ModeType.HANDLE)
    public void deletePlan(Long id) {
        if (!planRepository.existsById(id)) {
            throw new FwException(CommonErrorMessage.NOT_FOUND);
        }
        planRepository.deleteById(id);
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
}
