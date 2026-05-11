package com.thaca.auth.repositories;

import com.thaca.auth.domains.Tenant;
import com.thaca.auth.domains.projections.TenantInfoProjection;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long>, JpaSpecificationExecutor<Tenant> {
    boolean existsByCode(String code);

    @EntityGraph(attributePaths = { "plan" })
    Optional<Tenant> findByCode(String code);

    @Query(
        nativeQuery = true,
        value = """
        SELECT t.id, t.name, t.code, t.logo_url FROM auth.tenants t
        WHERE t.status = 'ACTIVE'
        ORDER BY t.updated_at DESC
        """
    )
    List<TenantInfoProjection> findAllActiveTenants();

    @Query(
        nativeQuery = true,
        value = """
        SELECT t.id, t.name, t.code, t.logo_url FROM auth.tenants t
        ORDER BY t.updated_at DESC
        """
    )
    List<TenantInfoProjection> findAllTenants();

    @EntityGraph(attributePaths = { "plan" })
    List<Tenant> findAllByIdIn(Collection<Long> ids);
}
