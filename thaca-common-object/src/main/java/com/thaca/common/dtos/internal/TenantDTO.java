package com.thaca.common.dtos.internal;

import com.thaca.common.dtos.BaseAuditResponse;
import com.thaca.common.enums.PlanType;
import com.thaca.common.enums.TenantStatus;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDTO extends BaseAuditResponse {

    private Long id;
    private String code;
    private String name;
    private String domain;
    private TenantStatus status;
    private Long planId;
    private PlanType planType;
    private LocalDateTime expiresAt;
    private String contactEmail;
    private String logoUrl;
}
