package com.thaca.notification.domains;

import com.thaca.common.enums.CommonStatus;
import com.thaca.framework.blocking.starter.configs.audit.BaseEntityAudit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "mail_configs", schema = "notification")
public class MailConfig extends BaseEntityAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "config_code")
    private String configCode;

    @Column(name = "description")
    private String description;

    @Column(name = "from_name")
    private String fromName;

    @Column(name = "from_email")
    private String fromEmail;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_auth")
    @Builder.Default
    private Boolean isAuth = true;

    @Column(name = "is_starttls")
    @Builder.Default
    private Boolean isStarttls = true;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status")
    private CommonStatus status = CommonStatus.ACTIVE;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;
}
