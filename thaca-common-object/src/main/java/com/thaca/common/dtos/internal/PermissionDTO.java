package com.thaca.common.dtos.internal;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {

    private String code;
    private String description;
    private String roleDescription;
    private String roleCode;
}
