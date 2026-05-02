package com.thaca.common.dtos.internal;

import com.thaca.common.enums.PermissionEffect;
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
    private PermissionEffect effect;
}
