package com.thaca.notification.repositories;

import com.thaca.common.enums.CommonStatus;
import com.thaca.notification.domains.MailConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MailConfigRepository extends JpaRepository<MailConfig, Long> {
    Optional<MailConfig> findFirstByTenantIdAndConfigCodeAndStatus(
        String tenantId,
        String configCode,
        CommonStatus status
    );

    Optional<MailConfig> findFirstByConfigCodeAndStatus(String configCode, CommonStatus status);

    Optional<MailConfig> findFirstByTenantIdAndStatusOrderByIsDefaultDescIdAsc(String tenantId, CommonStatus status);

    Optional<MailConfig> findFirstByTenantIdAndIsDefaultTrueAndStatus(String tenantId, CommonStatus status);

    Optional<MailConfig> findFirstByTenantIdIsNullAndIsDefaultTrueAndStatus(CommonStatus status);

    Optional<MailConfig> findFirstByTenantIdIsNullAndStatusOrderByIsDefaultDescIdAsc(CommonStatus status);

    @Modifying
    @Query("UPDATE MailConfig m SET m.isDefault = false WHERE m.tenantId = :tenantId AND m.id <> :excludeId")
    void clearDefaultForTenant(String tenantId, Long excludeId);

    @Modifying
    @Query("UPDATE MailConfig m SET m.isDefault = false WHERE m.tenantId IS NULL AND m.id <> :excludeId")
    void clearDefaultForSystem(Long excludeId);
}
