package com.thaca.notification.domains;

import com.thaca.common.enums.NotificationChannel;
import com.thaca.framework.blocking.starter.configs.audit.BaseEntityAudit;
import jakarta.persistence.*;
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
@Table(name = "notification_routing_configs", schema = "notification")
public class NotificationRoutingConfig extends BaseEntityAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "notification_type", nullable = false)
    private String notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    @Column(name = "mail_config_code")
    private String mailConfigCode;

    @Column(name = "template_code")
    private String templateCode;

    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 100;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;
}
