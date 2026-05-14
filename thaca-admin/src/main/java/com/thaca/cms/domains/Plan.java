package com.thaca.cms.domains;

import com.thaca.common.enums.CommonStatus;
import com.thaca.common.enums.PlanType;
import com.thaca.framework.blocking.starter.configs.audit.BaseEntityAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "plans", schema = "cms")
public class Plan extends BaseEntityAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "plan_seq")
    @SequenceGenerator(name = "plan_seq", allocationSize = 1, sequenceName = "cms.plan_seq")
    private Long id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private PlanType type;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CommonStatus status;
}
