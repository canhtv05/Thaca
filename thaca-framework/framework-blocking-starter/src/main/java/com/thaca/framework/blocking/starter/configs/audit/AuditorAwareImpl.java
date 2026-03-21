package com.thaca.framework.blocking.starter.configs.audit;

import com.thaca.framework.core.security.SecurityUtils;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(SecurityUtils.getCurrentUsername().orElse("SYSTEM"));
    }
}
