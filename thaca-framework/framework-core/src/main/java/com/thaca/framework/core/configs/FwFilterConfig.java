package com.thaca.framework.core.configs;

import com.thaca.framework.core.filter.FwFilter;
import com.thaca.framework.core.filter.IdempotencyGuard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FwFilterConfig {

    @Bean
    public FwFilter fwFilter(IdempotencyGuard guard) {
        return new FwFilter(guard);
    }
}
