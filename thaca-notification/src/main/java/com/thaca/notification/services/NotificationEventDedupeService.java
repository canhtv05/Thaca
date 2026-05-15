package com.thaca.notification.services;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventDedupeService {

    private static final String KEY_PREFIX = "notification:processed:";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedissonClient redissonClient;

    public boolean tryMarkProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return true;
        }
        RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + eventId, StringCodec.INSTANCE);
        boolean firstTime = bucket.setIfAbsent("1", TTL);
        if (!firstTime) {
            log.info("[NotificationEventDedupe] Skip duplicate eventId={}", eventId);
        }
        return firstTime;
    }
}
