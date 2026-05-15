package com.thaca.notification.repositories;

import com.thaca.common.enums.CommonStatus;
import com.thaca.notification.domains.MailConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailConfigRepository extends JpaRepository<MailConfig, Long> {
    // Tìm config theo tenant, ưu tiên lấy config đang active
    Optional<MailConfig> findFirstByTenantIdAndStatus(String tenantId, CommonStatus status);

    // Tìm config theo mã cấu hình (ví dụ: MY_GMAIL)
    Optional<MailConfig> findFirstByConfigCodeAndStatus(String configCode, CommonStatus status);

    // Fallback: Config mặc định của hệ thống
    Optional<MailConfig> findFirstByTenantIdIsNullAndStatus(CommonStatus status);
}
