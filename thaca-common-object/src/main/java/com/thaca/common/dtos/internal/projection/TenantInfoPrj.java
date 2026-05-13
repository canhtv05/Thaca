package com.thaca.common.dtos.internal.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantInfoPrj {

    private Long id;
    private String name;
    private String code;
    private String domain;
    private String status;
    private Long planId;
    private String expiresAt;
    private String contactEmail;
    private String logoUrl;
    private Long version;
    private PlanInfoPrj plan;
}
