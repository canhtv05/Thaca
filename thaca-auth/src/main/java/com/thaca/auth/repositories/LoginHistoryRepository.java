package com.thaca.auth.repositories;

import com.thaca.auth.domains.LoginHistory;
import com.thaca.auth.enums.LoginStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginHistoryRepository
    extends JpaRepository<LoginHistory, Long>, JpaSpecificationExecutor<LoginHistory>
{
    boolean existsByUser_IdAndDeviceIdAndStatus(Long userId, String deviceId, LoginStatus status);

    boolean existsBySystemUser_IdAndDeviceIdAndStatus(Long systemUserId, String deviceId, LoginStatus status);
}
