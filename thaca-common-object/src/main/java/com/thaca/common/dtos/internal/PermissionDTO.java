package com.thaca.common.dtos.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {

    private String code;
    private String description;

    @JsonIgnore
    private String roleCode;
}
