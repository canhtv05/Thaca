package com.thaca.auth.repositories;

import com.thaca.auth.domains.SystemCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemCredentialRepository extends JpaRepository<SystemCredential, String> {
    @EntityGraph(attributePaths = { "roles", "roles.permissions", "credentialPermissions.permission", "systemUser" })
    Optional<SystemCredential> findByUsername(String username);
}
