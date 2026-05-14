package com.thaca.admin.mappers;

import com.thaca.admin.domains.Plan;
import com.thaca.admin.domains.projection.PlanInfoProjection;
import com.thaca.common.dtos.internal.PlanDTO;
import com.thaca.common.dtos.internal.projection.PlanInfoPrj;
import com.thaca.framework.core.utils.DateUtils;

public class PlanMapper {

    private PlanMapper() {}

    public static PlanDTO fromEntity(Plan plan) {
        if (plan == null) {
            return null;
        }
        return PlanDTO.builder()
            .id(plan.getId())
            .code(plan.getCode())
            .name(plan.getName())
            .type(plan.getType())
            .maxUsers(plan.getMaxUsers())
            .status(plan.getStatus())
            .createdBy(plan.getCreatedBy())
            .updatedBy(plan.getUpdatedBy())
            .createdAt(DateUtils.dateToString(plan.getCreatedAt()))
            .updatedAt(DateUtils.dateToString(plan.getUpdatedAt()))
            .build();
    }

    public static PlanInfoPrj fromPrj(PlanInfoProjection plan) {
        if (plan == null) {
            return null;
        }
        return PlanInfoPrj.builder().id(plan.getId()).code(plan.getCode()).name(plan.getName()).build();
    }
}
