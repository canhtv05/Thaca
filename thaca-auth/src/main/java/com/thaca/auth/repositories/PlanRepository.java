package com.thaca.auth.repositories;

import com.thaca.auth.domains.Plan;
import com.thaca.auth.domains.projections.PlanInfoProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long>, JpaSpecificationExecutor<Plan> {
    Optional<Plan> findByCode(String code);

    boolean existsByCode(String code);

    @Query(
        nativeQuery = true,
        value = """
        SELECT p.id, p.name, p.code FROM auth.plans p
        WHERE p.status = 'ACTIVE'
        ORDER BY p.updated_at DESC
        """
    )
    List<PlanInfoProjection> findAllActivePlansOrderByUpdatedAtDesc();

    @Query(
        nativeQuery = true,
        value = """
        SELECT p.id, p.name, p.code FROM auth.plans p
        ORDER BY p.updated_at DESC
        """
    )
    List<PlanInfoProjection> findAllPlanInfo();
}
