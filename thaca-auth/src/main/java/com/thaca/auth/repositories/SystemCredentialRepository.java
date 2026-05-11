package com.thaca.auth.repositories;

import com.thaca.auth.domains.SystemCredential;
import com.thaca.auth.domains.SystemUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemCredentialRepository
    extends JpaRepository<SystemCredential, String>, JpaSpecificationExecutor<SystemCredential>
{
    @Query(
        value = """
        SELECT p.code
        FROM auth.system_credential_permissions scp
        LEFT JOIN auth.permissions p ON scp.permission_code = p.code
        WHERE scp.credential_id = :credentialId
          AND UPPER(TRIM(scp.effect)) = 'DENY'
        """,
        nativeQuery = true
    )
    List<String> findDeniedPermissionCodes(@Param("credentialId") String credentialId);

    @EntityGraph(attributePaths = { "roles", "roles.permissions", "credentialPermissions.permission", "systemUser" })
    Optional<SystemCredential> findByUsername(String username);

    @Query(
        value = """
        SELECT sc.* FROM auth.system_credentials sc
        JOIN auth.system_users su ON sc.system_user_id = su.id
        WHERE sc.username = :username AND su.tenant_id = :tenantId
        """,
        nativeQuery = true
    )
    Optional<SystemCredential> findByUsernameAndTenantId(
        @Param("username") String username,
        @Param("tenantId") Long tenantId
    );

    Optional<SystemCredential> findBySystemUser(SystemUser systemUser);
}
