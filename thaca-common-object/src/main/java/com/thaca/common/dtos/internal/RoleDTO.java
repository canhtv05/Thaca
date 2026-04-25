package com.thaca.common.dtos.internal;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private String code;
    private String name;
    private String description;
}
