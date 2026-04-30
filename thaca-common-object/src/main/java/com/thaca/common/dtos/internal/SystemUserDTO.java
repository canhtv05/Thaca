package com.thaca.common.dtos.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thaca.common.dtos.BaseAuditResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private Set<String> roles;
    private TenantDTO tenantInfo;
    private Map<String, Boolean> permissions;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Long> tenantIds;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String lockReason;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> roleCodes;
}
