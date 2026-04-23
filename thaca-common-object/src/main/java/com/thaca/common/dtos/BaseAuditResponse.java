package com.thaca.common.dtos;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class BaseAuditResponse {

    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private String updatedBy;
}
