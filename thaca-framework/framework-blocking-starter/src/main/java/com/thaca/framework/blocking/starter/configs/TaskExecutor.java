package com.thaca.framework.blocking.starter.configs;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
public class TaskExecutor extends ThreadPoolTaskExecutor {

    public TaskExecutor() {
        setCorePoolSize(4);
        setMaxPoolSize(8);
        setQueueCapacity(100);
        setThreadNamePrefix("thaca-async-");
        setWaitForTasksToCompleteOnShutdown(true);
        setAwaitTerminationSeconds(30);
        initialize();
        log.info("[TaskExecutor] Initialized: core=4, max=8, queue=100");
    }

    @PreDestroy
    public void onShutdown() {
        log.info("[TaskExecutor] Shutting down, waiting for pending tasks...");
        shutdown();
        log.info("[TaskExecutor] Shutdown complete.");
    }
}
