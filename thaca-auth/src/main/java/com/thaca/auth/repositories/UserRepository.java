package com.thaca.auth.repositories;

import com.thaca.auth.domains.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Override
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @Override
    List<User> findAll(Specification<User> spec, Sort sort);

    @Query(
        value = """
        SELECT ut.tenant_id AS tenantId, COUNT(u.id) AS count
        FROM auth.users u
        JOIN auth.user_tenants ut ON u.id = ut.user_id
        WHERE ut.tenant_id IN (:tenantIds)
        GROUP BY ut.tenant_id
        """,
        nativeQuery = true
    )
    List<Map<String, Object>> countByTenantIds(@Param("tenantIds") Collection<Long> tenantIds);

    boolean existsByEmail(String email);

    @NonNull
    Optional<User> findById(@NonNull Long id);

    Boolean existsUserByUsername(String username);

    @Query(
        value = "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM auth.users u JOIN auth.user_tenants ut ON u.id = ut.user_id WHERE u.username = :username AND ut.tenant_id = :tenantId",
        nativeQuery = true
    )
    boolean existsByUsernameAndTenantId(@Param("username") String username, @Param("tenantId") Long tenantId);

    @Query(
        value = "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM auth.users u JOIN auth.user_tenants ut ON u.id = ut.user_id WHERE u.email = :email AND ut.tenant_id = :tenantId",
        nativeQuery = true
    )
    boolean existsByEmailAndTenantId(@Param("email") String email, @Param("tenantId") Long tenantId);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAllByUsernameIn(Collection<String> usernames);

    List<User> findAllByEmailIn(Collection<String> emails);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query(
        value = "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM auth.users u JOIN auth.user_tenants ut ON u.id = ut.user_id WHERE u.email = :email AND ut.tenant_id IN (:tenantIds)",
        nativeQuery = true
    )
    boolean existsByEmailAndTenantIds(@Param("email") String email, @Param("tenantIds") Collection<Long> tenantIds);

    @Query(
        value = "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM auth.users u JOIN auth.user_tenants ut ON u.id = ut.user_id WHERE u.username = :username AND ut.tenant_id IN (:tenantIds)",
        nativeQuery = true
    )
    boolean existsByUsernameAndTenantIds(
        @Param("username") String username,
        @Param("tenantIds") Collection<Long> tenantIds
    );

    @Query(
        value = """
        SELECT u.* FROM auth.users u
        JOIN auth.user_tenants ut ON u.id = ut.user_id
        WHERE u.username IN (:usernames) AND ut.tenant_id IN (:tenantIds)
        """,
        nativeQuery = true
    )
    List<User> findByUsernamesAndTenantIds(
        @Param("usernames") Collection<String> usernames,
        @Param("tenantIds") Collection<Long> tenantIds
    );

    @Modifying
    @Query(value = "DELETE FROM auth.user_tenants WHERE tenant_id = :tenantId", nativeQuery = true)
    void deleteUserTenantsByTenantId(@Param("tenantId") Long tenantId);
}
