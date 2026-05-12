package com.thaca.common.dtos.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thaca.common.dtos.BaseAuditResponse;
import com.thaca.common.dtos.internal.projection.TenantInfoPrj;
import java.util.List;
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
public class UserDTO extends BaseAuditResponse {

    private Long id;
    private String fullname;
    private String username;

    @JsonIgnore
    private String password;

    private String email;
    private Boolean isActivated;
    private Boolean isLocked;
    private List<Long> tenantIds;
    private List<TenantInfoPrj> tenants;
}
