package com.thaca.framework.blocking.starter.configs.audit;

import com.thaca.framework.core.security.SecurityUtils;
import java.util.Optional;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Component;

@Component
@AutoConfiguration
@ConditionalOnClass(EnableJpaAuditing.class)
@EnableJpaAuditing
@ConditionalOnBean(SecurityUtils.class)
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public @NonNull Optional<String> getCurrentAuditor() {
        return StringUtils.defaultIfBlank(SecurityUtils.getCurrentUsername(), "SYSTEM").describeConstable();
    }
}
