package com.thaca.auth.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.proxy.HibernateProxy;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "roles", schema = "auth")
public class Role extends BaseTenantEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "code", length = 50, unique = true, nullable = false)
    private String code;

    @Column(name = "description", nullable = false)
    private String description;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "role_permissions",
        joinColumns = { @JoinColumn(name = "role_code", referencedColumnName = "code") },
        inverseJoinColumns = { @JoinColumn(name = "permission_code", referencedColumnName = "code") }
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        Class<?> oEffectiveClass =
            object instanceof HibernateProxy
                ? ((HibernateProxy) object).getHibernateLazyInitializer().getPersistentClass()
                : object.getClass();
        Class<?> thisEffectiveClass =
            this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Role role = (Role) object;
        return getCode() != null && Objects.equals(getCode(), role.getCode());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }
}
