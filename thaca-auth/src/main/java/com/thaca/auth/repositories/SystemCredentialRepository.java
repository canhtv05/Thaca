package com.thaca.auth.repositories;

import com.thaca.auth.domains.SystemCredential;
import com.thaca.auth.domains.SystemUser;
import com.thaca.auth.repositories.projection.DuplicateCheckPrj;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemCredentialRepository
    extends JpaRepository<SystemCredential, Long>, JpaSpecificationExecutor<SystemCredential>
{
    @Override
    @EntityGraph(
        attributePaths = {
            "systemUser",
            "systemUser.tenantIds",
            "roles",
            "roles.permissions",
            "credentialPermissions",
            "credentialPermissions.permission"
        }
    )
    Page<SystemCredential> findAll(Specification<SystemCredential> spec, Pageable pageable);

    @Override
    @EntityGraph(
        attributePaths = {
            "systemUser",
            "systemUser.tenantIds",
            "roles",
            "roles.permissions",
            "credentialPermissions",
            "credentialPermissions.permission"
        }
    )
    List<SystemCredential> findAll(Specification<SystemCredential> spec, Sort sort);

    @Query(
        value = """
        SELECT p.code
        FROM auth.system_credential_permissions scp
        LEFT JOIN auth.permissions p ON scp.permission_code = p.code
        WHERE scp.system_user_id = :systemUserId
          AND UPPER(TRIM(scp.effect)) = 'DENY'
        """,
        nativeQuery = true
    )
    List<String> findDeniedPermissionCodes(@Param("systemUserId") Long systemUserId);

    @EntityGraph(
        attributePaths = {
            "roles", "roles.permissions", "credentialPermissions.permission", "systemUser", "systemUser.tenantIds"
        }
    )
    Optional<SystemCredential> findByUsername(String username);

    @Query(
        value = """
        SELECT sc.* FROM auth.system_credentials sc
        JOIN auth.system_users su ON sc.system_user_id = su.id
        JOIN auth.system_user_tenants sut ON su.id = sut.system_user_id
        WHERE sc.username = :username AND sut.tenant_id = :tenantId
        """,
        nativeQuery = true
    )
    Optional<SystemCredential> findByUsernameAndTenantId(
        @Param("username") String username,
        @Param("tenantId") Long tenantId
    );

    @Query(
        value = """
        SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END
        FROM auth.system_credentials sc
        JOIN auth.system_users su ON sc.system_user_id = su.id
        JOIN auth.system_user_tenants sut ON su.id = sut.system_user_id
        WHERE sc.username = :username AND sut.tenant_id IN (:tenantIds)
        """,
        nativeQuery = true
    )
    boolean existsByUsernameAndTenantIds(
        @Param("username") String username,
        @Param("tenantIds") Collection<Long> tenantIds
    );

    @Query(
        value = """
        SELECT sut.tenant_id AS tenantId, sc.username AS username
        FROM auth.system_credentials sc
        JOIN auth.system_users su ON sc.system_user_id = su.id
        JOIN auth.system_user_tenants sut ON su.id = sut.system_user_id
        WHERE sc.username = :username AND sut.tenant_id IN (:tenantIds)
        """,
        nativeQuery = true
    )
    List<DuplicateCheckPrj> findConflictingTenantsByUsername(
        @Param("username") String username,
        @Param("tenantIds") Collection<Long> tenantIds
    );

    Optional<SystemCredential> findBySystemUser(SystemUser systemUser);
}
