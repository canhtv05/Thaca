package com.thaca.auth.domains;

import com.thaca.common.enums.PermissionEffect;
import com.thaca.framework.blocking.starter.configs.audit.BaseEntityAudit;
import jakarta.persistence.*;
import java.util.Objects;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "system_credential_permissions", schema = "auth")
public class SystemCredentialPermission extends BaseEntityAudit {

    @EmbeddedId
    private SystemCredentialPermissionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("credentialId")
    @JoinColumn(name = "credential_id")
    private SystemCredential credential;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleCode")
    @JoinColumn(name = "role_code")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("permissionCode")
    @JoinColumn(name = "permission_code")
    private Permission permission;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "effect", nullable = false)
    private PermissionEffect effect = PermissionEffect.DENY;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SystemCredentialPermission that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Getter
    @Setter
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemCredentialPermissionId implements java.io.Serializable {

        @Column(name = "credential_id")
        private String credentialId;

        @Column(name = "role_code")
        private String roleCode;

        @Column(name = "permission_code")
        private String permissionCode;

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof SystemCredentialPermissionId that)) return false;
            return (
                Objects.equals(credentialId, that.credentialId) &&
                Objects.equals(roleCode, that.roleCode) &&
                Objects.equals(permissionCode, that.permissionCode)
            );
        }

        @Override
        public int hashCode() {
            return Objects.hash(credentialId, roleCode, permissionCode);
        }
    }
}
