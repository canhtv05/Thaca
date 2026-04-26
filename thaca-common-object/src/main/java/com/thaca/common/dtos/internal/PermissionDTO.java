package com.thaca.common.dtos.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
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
