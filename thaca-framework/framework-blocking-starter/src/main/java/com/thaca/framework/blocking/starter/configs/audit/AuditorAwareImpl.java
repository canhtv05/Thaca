package com.thaca.framework.blocking.starter.configs.audit;

import com.thaca.framework.core.security.SecurityUtils;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public @NullMarked Optional<String> getCurrentAuditor() {
        return StringUtils.defaultIfBlank(SecurityUtils.getCurrentUsername(), "SYSTEM").describeConstable();
    }
}
