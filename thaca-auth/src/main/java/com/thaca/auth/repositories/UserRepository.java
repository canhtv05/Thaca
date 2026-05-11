package com.thaca.auth.repositories;

import com.thaca.auth.domains.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Query(
        value = """
        SELECT u.tenant_id AS tenantId, COUNT(*) AS count
        FROM auth.users u
        WHERE u.tenant_id IN (:tenantIds)
        GROUP BY u.tenant_id
        """,
        nativeQuery = true
    )
    List<Map<String, Object>> countByTenantIds(@Param("tenantIds") Collection<Long> tenantIds);

    boolean existsByEmail(String email);

    @NonNull
    Optional<User> findById(@NonNull Long id);

    Boolean existsUserByUsername(String username);

    boolean existsByUsernameAndTenantId(String username, Long tenantId);

    boolean existsByEmailAndTenantId(String email, Long tenantId);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAllByUsernameIn(Collection<String> usernames);

    List<User> findAllByEmailIn(Collection<String> emails);
}
