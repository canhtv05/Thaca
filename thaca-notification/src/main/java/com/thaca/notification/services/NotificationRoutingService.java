package com.thaca.notification.services;

import com.thaca.common.enums.NotificationChannel;
import com.thaca.common.events.base.DomainEvent;
import com.thaca.common.events.base.EventMetadata;
import com.thaca.notification.domains.NotificationRoutingConfig;
import com.thaca.notification.repositories.NotificationRoutingConfigRepository;
import java.util.Objects;
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
        EventMetadata metadata = event.metadata();
        String tenantId = metadata.getTenantId();
        String explicitConfigCode = metadata.getMailConfigCode();
        String templateCode = metadata.getTemplateCode();
        boolean useSystemDefault = metadata.getUseDefaultConfig();
        String notificationType = event.notificationType();

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
            String routeConfigCode = config.getMailConfigCode();
            String routeTemplateCode = defaultTemplate(config.getTemplateCode(), templateCode);
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

    private String defaultTemplate(String primaryTemplate) {
        return defaultTemplate(primaryTemplate, null);
    }

    private String defaultTemplate(String primaryTemplate, String fallbackTemplate) {
        if (primaryTemplate != null) {
            return primaryTemplate;
        }
        return Objects.requireNonNullElse(fallbackTemplate, DEFAULT_TEMPLATE_CODE);
    }
}
