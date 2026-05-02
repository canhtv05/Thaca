package com.thaca.auth.repositories;

import com.thaca.auth.domains.UserLockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLockHistoryRepository
    extends JpaRepository<UserLockHistory, Long>, JpaSpecificationExecutor<UserLockHistory> {}
