package com.thaca.framework.core.filter;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdempotencyGuard implements DuplicateRequestGuard {

    private static final String LOCK_PREFIX = "fw:req-lock:";
    private static final long TTL = 30;
    private final RedissonClient redissonClient;

    @Override
    public RLock tryAcquire(String key) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);
        try {
            boolean acquired = lock.tryLock(0, TTL, TimeUnit.SECONDS);
            return acquired ? lock : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public void release(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
