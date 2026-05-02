package com.thaca.common.dtos.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thaca.common.dtos.BaseAuditResponse;
import com.thaca.common.enums.PermissionEffect;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemUserDTO extends BaseAuditResponse {

    private Long id;
    private Long tenantId;
    private String username;
    private String email;
    private String fullname;
    private Boolean isActivated;
    private Boolean isLocked;
    private Boolean isSuperAdmin;
    private String avatarUrl;
    private TenantDTO tenantInfo;
    private Map<String, Map<String, PermissionEffect>> roles;

    private String password;
    private List<Long> tenantIds;
    private String lockReason;
}
