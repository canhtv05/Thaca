package com.thaca.framework.blocking.starter.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncConfig {

    @Bean
    TaskExecutor taskExecutor() {
        return new TaskExecutor();
    }
}
