package com.thaca.auth.repositories;

import com.thaca.auth.domains.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    boolean existsByEmail(String email);

    @NonNull
    Optional<User> findById(@NonNull Long id);

    Boolean existsUserByUsername(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAllByUsernameIn(Collection<String> usernames);

    List<User> findAllByEmailIn(Collection<String> emails);
}
