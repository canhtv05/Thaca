package com.thaca.auth.domains;

import com.thaca.framework.blocking.starter.configs.audit.BaseEntityAudit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "permissions", schema = "auth")
public class Permission extends BaseEntityAudit {

    @Id
    @Column(name = "code", length = 50, unique = true, nullable = false)
    private String code;

    @Column(name = "description", nullable = false)
    private String description;
}
