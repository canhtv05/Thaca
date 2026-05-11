package com.thaca.auth.repositories;

import com.thaca.auth.domains.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, Long>, JpaSpecificationExecutor<SystemUser> {
    boolean existsByEmail(String email);

    boolean existsByEmailAndTenantId(String email, Long tenantId);
}
