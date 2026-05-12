package com.thaca.cms.repositories;

import com.thaca.cms.domains.Tenant;
import com.thaca.cms.domains.projection.TenantInfoProjection;
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
public interface TenantRepository extends JpaRepository<Tenant, Long>, JpaSpecificationExecutor<Tenant> {
    boolean existsByCode(String code);

    @EntityGraph(attributePaths = { "plan" })
    Optional<Tenant> findByCode(String code);

    @Query(
        nativeQuery = true,
        value = """
        SELECT t.id, t.name, t.code, t.logo_url FROM cms.tenants t
        WHERE t.status = 'ACTIVE' AND t.deleted_at IS NULL
        ORDER BY t.updated_at DESC
        """
    )
    List<TenantInfoProjection> findAllActiveTenants();

    @Query(
        nativeQuery = true,
        value = """
        SELECT t.id, t.name, t.code, t.logo_url FROM cms.tenants t
        WHERE t.deleted_at IS NULL
        ORDER BY t.updated_at DESC
        """
    )
    List<TenantInfoProjection> findAllTenants();

    @EntityGraph(attributePaths = { "plan" })
    List<Tenant> findAllByIdIn(Collection<Long> ids);

    @Query(nativeQuery = true, value = "SELECT * FROM cms.tenants WHERE id = :id")
    Optional<Tenant> findSoftDeletedById(@Param("id") Long id);

    @Query(
        nativeQuery = true,
        value = """
        SELECT t.id, t.name, t.code, t.logo_url FROM cms.tenants t
        WHERE t.id IN :ids AND t.deleted_at IS NULL
        """
    )
    List<TenantInfoProjection> findTenantsByIds(Collection<Long> ids);
}
