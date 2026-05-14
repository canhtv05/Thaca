package com.thaca.auth.repositories;

import com.thaca.auth.domains.SystemUser;
import com.thaca.auth.repositories.projection.DuplicateCheckPrj;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, Long>, JpaSpecificationExecutor<SystemUser> {
    @Override
    @EntityGraph(attributePaths = { "tenantIds" })
    Optional<SystemUser> findById(Long id);

    boolean existsByEmail(String email);

    Optional<SystemUser> findByEmail(String email);

    @Query(
        value = "SELECT CASE WHEN COUNT(su) > 0 THEN true ELSE false END FROM auth.system_users su JOIN auth.system_user_tenants sut ON su.id = sut.system_user_id WHERE su.email = :email AND sut.tenant_id = :tenantId",
        nativeQuery = true
    )
    boolean existsByEmailAndTenantId(@Param("email") String email, @Param("tenantId") Long tenantId);

    @Query(
        value = "SELECT CASE WHEN COUNT(su) > 0 THEN true ELSE false END FROM auth.system_users su JOIN auth.system_user_tenants sut ON su.id = sut.system_user_id WHERE su.email = :email AND sut.tenant_id IN (:tenantIds)",
        nativeQuery = true
    )
    boolean existsByEmailAndTenantIds(@Param("email") String email, @Param("tenantIds") Collection<Long> tenantIds);

    @Query(
        value = "SELECT CASE WHEN COUNT(su) > 0 THEN true ELSE false END FROM auth.system_users su JOIN auth.system_user_tenants sut ON su.id = sut.system_user_id WHERE su.email = :email AND sut.tenant_id IN (:tenantIds) AND su.id != :id",
        nativeQuery = true
    )
    boolean existsByEmailAndTenantIdsAndIdNot(
        @Param("email") String email,
        @Param("tenantIds") Collection<Long> tenantIds,
        @Param("id") Long id
    );

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query(
        value = """
        SELECT sut.tenant_id AS tenantId, su.email AS email
        FROM auth.system_users su
        JOIN auth.system_user_tenants sut ON su.id = sut.system_user_id
        WHERE su.email = :email AND sut.tenant_id IN (:tenantIds)
        """,
        nativeQuery = true
    )
    List<DuplicateCheckPrj> findConflictingTenantsByEmail(
        @Param("email") String email,
        @Param("tenantIds") Collection<Long> tenantIds
    );

    @Query(
        value = """
        SELECT sut.tenant_id AS tenantId, su.email AS email
        FROM auth.system_users su
        JOIN auth.system_user_tenants sut ON su.id = sut.system_user_id
        WHERE su.email = :email AND sut.tenant_id IN (:tenantIds) AND su.id != :id
        """,
        nativeQuery = true
    )
    List<DuplicateCheckPrj> findConflictingTenantsByEmailAndIdNot(
        @Param("email") String email,
        @Param("tenantIds") Collection<Long> tenantIds,
        @Param("id") Long id
    );
}
