package com.thaca.framework.core.filter;

import org.redisson.api.RLock;

public interface DuplicateRequestGuard {
    RLock tryAcquire(String key);

    void release(RLock key);
}
