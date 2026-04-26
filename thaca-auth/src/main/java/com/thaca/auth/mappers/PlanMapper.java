package com.thaca.auth.mappers;

import com.thaca.auth.domains.Plan;
import com.thaca.common.dtos.internal.PlanDTO;
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
}
