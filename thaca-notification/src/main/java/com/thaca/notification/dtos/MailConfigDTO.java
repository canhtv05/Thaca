package com.thaca.notification.dtos;

import com.thaca.common.dtos.BaseAuditResponse;
import com.thaca.common.enums.CommonStatus;
import com.thaca.framework.core.utils.DateUtils;
import com.thaca.notification.domains.MailConfig;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class MailConfigDTO extends BaseAuditResponse {

    private Long id;
    private String tenantId;
    private String configCode;
    private String description;
    private String fromName;
    private String fromEmail;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Boolean isAuth;
    private Boolean isStarttls;
    private CommonStatus status;
    private Boolean isDefault;

    public static MailConfigDTO fromEntity(MailConfig mailConfig) {
        if (mailConfig == null) return null;
        return MailConfigDTO.builder()
            .id(mailConfig.getId())
            .tenantId(mailConfig.getTenantId())
            .configCode(mailConfig.getConfigCode())
            .description(mailConfig.getDescription())
            .fromName(mailConfig.getFromName())
            .fromEmail(mailConfig.getFromEmail())
            .host(mailConfig.getHost())
            .port(mailConfig.getPort())
            .username(mailConfig.getUsername())
            .password(null)
            .isAuth(mailConfig.getIsAuth())
            .isStarttls(mailConfig.getIsStarttls())
            .status(mailConfig.getStatus())
            .isDefault(mailConfig.getIsDefault())
            .createdAt(DateUtils.dateToString(mailConfig.getCreatedAt()))
            .updatedAt(DateUtils.dateToString(mailConfig.getUpdatedAt()))
            .createdBy(mailConfig.getCreatedBy())
            .updatedBy(mailConfig.getUpdatedBy())
            .build();
    }
}
