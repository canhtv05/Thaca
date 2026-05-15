package com.thaca.notification.domains;

import com.thaca.common.enums.CommonStatus;
import com.thaca.framework.blocking.starter.configs.audit.BaseEntityAudit;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "mail_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MailConfig extends BaseEntityAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "config_code")
    private String configCode;

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
    @Column(name = "status")
    @Builder.Default
    private CommonStatus status = CommonStatus.ACTIVE;
}
