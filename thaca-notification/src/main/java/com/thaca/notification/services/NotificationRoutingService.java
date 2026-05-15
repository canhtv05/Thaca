package com.thaca.notification.services;

import com.thaca.common.enums.NotificationChannel;
import com.thaca.common.events.base.DomainEvent;
import com.thaca.notification.domains.NotificationRoutingConfig;
import com.thaca.notification.repositories.NotificationRoutingConfigRepository;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRoutingService {

    private static final String DEFAULT_TEMPLATE_CODE = "otp-email";

    private final NotificationRoutingConfigRepository routingConfigRepository;
    private final DynamicMailSenderService dynamicMailSenderService;

    public ResolvedEmailDispatch resolveEmailDispatch(DomainEvent event) {
        Map<String, Object> metadata = Optional.ofNullable(event.metadata()).orElse(Map.of());
        String tenantId = extractTenantId(metadata);
        String explicitConfigCode = trimToNull(asString(metadata.get("configCode")));
        String templateCode = trimToNull(asString(metadata.get("templateCode")));
        boolean useSystemDefault = Boolean.TRUE.equals(metadata.get("useDefaultConfig"));
        String notificationType = event.eventType();

        if (useSystemDefault) {
            return new ResolvedEmailDispatch(
                dynamicMailSenderService.getLocalMailSender(),
                dynamicMailSenderService.getLocalSenderAddress(),
                defaultTemplate(templateCode),
                notificationType,
                tenantId
            );
        }

        if (explicitConfigCode != null) {
            return new ResolvedEmailDispatch(
                dynamicMailSenderService.getMailSender(tenantId, explicitConfigCode),
                dynamicMailSenderService.getSenderAddress(tenantId, explicitConfigCode),
                defaultTemplate(templateCode),
                notificationType,
                tenantId
            );
        }

        Optional<NotificationRoutingConfig> route = resolveRoute(tenantId, notificationType, NotificationChannel.EMAIL);
        if (route.isPresent()) {
            NotificationRoutingConfig config = route.get();
            String routeConfigCode = trimToNull(config.getMailConfigCode());
            String routeTemplateCode = defaultTemplate(trimToNull(config.getTemplateCode()), templateCode);
            if (routeConfigCode != null) {
                return new ResolvedEmailDispatch(
                    dynamicMailSenderService.getMailSender(tenantId, routeConfigCode),
                    dynamicMailSenderService.getSenderAddress(tenantId, routeConfigCode),
                    routeTemplateCode,
                    notificationType,
                    tenantId
                );
            }
            return new ResolvedEmailDispatch(
                dynamicMailSenderService.getTenantMailSender(tenantId),
                dynamicMailSenderService.getTenantSenderAddress(tenantId),
                routeTemplateCode,
                notificationType,
                tenantId
            );
        }

        return new ResolvedEmailDispatch(
            dynamicMailSenderService.getTenantMailSender(tenantId),
            dynamicMailSenderService.getTenantSenderAddress(tenantId),
            defaultTemplate(templateCode),
            notificationType,
            tenantId
        );
    }

    private Optional<NotificationRoutingConfig> resolveRoute(
        String tenantId,
        String notificationType,
        NotificationChannel channel
    ) {
        if (tenantId != null) {
            Optional<NotificationRoutingConfig> tenantSpecific =
                routingConfigRepository.findFirstByTenantIdAndNotificationTypeAndChannelAndEnabledTrueOrderByPriorityAscIdAsc(
                    tenantId,
                    notificationType,
                    channel
                );
            if (tenantSpecific.isPresent()) {
                return tenantSpecific;
            }

            Optional<NotificationRoutingConfig> tenantDefault =
                routingConfigRepository.findFirstByTenantIdAndChannelAndIsDefaultTrueAndEnabledTrueOrderByPriorityAscIdAsc(
                    tenantId,
                    channel
                );
            if (tenantDefault.isPresent()) {
                return tenantDefault;
            }
        }

        Optional<NotificationRoutingConfig> systemSpecific =
            routingConfigRepository.findFirstByTenantIdIsNullAndNotificationTypeAndChannelAndEnabledTrueOrderByPriorityAscIdAsc(
                notificationType,
                channel
            );
        if (systemSpecific.isPresent()) {
            return systemSpecific;
        }

        return routingConfigRepository.findFirstByTenantIdIsNullAndChannelAndIsDefaultTrueAndEnabledTrueOrderByPriorityAscIdAsc(
            channel
        );
    }

    private String extractTenantId(Map<String, Object> metadata) {
        String tenantId = trimToNull(asString(metadata.get("tenantId")));
        if (tenantId != null) {
            return tenantId;
        }
        return trimToNull(asString(metadata.get("tenant_id")));
    }

    private String defaultTemplate(String primaryTemplate) {
        return defaultTemplate(primaryTemplate, null);
    }

    private String defaultTemplate(String primaryTemplate, String fallbackTemplate) {
        if (primaryTemplate != null) {
            return primaryTemplate;
        }
        if (fallbackTemplate != null) {
            return fallbackTemplate;
        }
        return DEFAULT_TEMPLATE_CODE;
    }

    private String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
