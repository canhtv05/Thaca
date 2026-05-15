package com.thaca.notification.repositories;

import com.thaca.common.enums.NotificationChannel;
import com.thaca.notification.domains.NotificationRoutingConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRoutingConfigRepository extends JpaRepository<NotificationRoutingConfig, Long> {
    Optional<
        NotificationRoutingConfig
    > findFirstByTenantIdAndNotificationTypeAndChannelAndEnabledTrueOrderByPriorityAscIdAsc(
        String tenantId,
        String notificationType,
        NotificationChannel channel
    );

    Optional<
        NotificationRoutingConfig
    > findFirstByTenantIdIsNullAndNotificationTypeAndChannelAndEnabledTrueOrderByPriorityAscIdAsc(
        String notificationType,
        NotificationChannel channel
    );

    Optional<
        NotificationRoutingConfig
    > findFirstByTenantIdAndChannelAndIsDefaultTrueAndEnabledTrueOrderByPriorityAscIdAsc(
        String tenantId,
        NotificationChannel channel
    );

    Optional<
        NotificationRoutingConfig
    > findFirstByTenantIdIsNullAndChannelAndIsDefaultTrueAndEnabledTrueOrderByPriorityAscIdAsc(
        NotificationChannel channel
    );
}
