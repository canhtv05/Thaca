package com.thaca.common.dtos.internal;

import com.thaca.common.dtos.BaseAuditResponse;
import com.thaca.common.enums.CommonStatus;
import com.thaca.common.enums.PlanType;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDTO extends BaseAuditResponse {

    private Long id;
    private String code;
    private String name;
    private PlanType type;
    private Integer maxUsers;
    private CommonStatus status;
}
