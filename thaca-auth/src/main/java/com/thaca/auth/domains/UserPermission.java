package com.thaca.auth.domains;

import com.thaca.auth.enums.PermissionAction;
import com.thaca.framework.blocking.starter.configs.audit.BaseEntityAudit;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "user_permission", schema = "auth")
public class UserPermission extends BaseEntityAudit {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "permission_code", nullable = false)
    private String permissionCode;

    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private PermissionAction action;

    @ManyToOne
    @JoinColumn(name = "permission_code", updatable = false, insertable = false)
    private Permission permission;
}
