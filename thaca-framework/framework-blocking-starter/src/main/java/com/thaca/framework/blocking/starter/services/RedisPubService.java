package com.thaca.framework.blocking.starter.services;

import com.thaca.common.socket.WsMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPubService {

    private final Redisson redisson;
    private final Environment environment;

    public void publish(WsMessage message) {
        try {
            String envRunning = environment.getActiveProfiles()[0];
            RTopic topic = redisson.getTopic(envRunning + ":ws:framework:event");
            topic.publish(message);
        } catch (IllegalArgumentException e) {
            log.error("[RedisPubService] publish()]:: ", e);
        }
    }
}
