package com.thaca.framework.blocking.starter.services;

import com.thaca.common.dtos.UserSession;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.blocking.starter.configs.cache.RedisCacheService;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.utils.FwUtils;
import com.thaca.framework.core.utils.JsonF;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final FrameworkProperties frameworkProperties;
    private final SessionStore sessionStore;
    private final RedisCacheService redisService;

    public <T extends UserSession> void cacheUserSession(T userSession) {
        try {
            String data = JsonF.toJson(userSession);
            long ttl = this.frameworkProperties.getSecurity().getValidDurationInSeconds() + 300L;
            var keyUser = sessionStore.getKeyUser(userSession.getUsername(), userSession.getChannel());
            redisService.set(keyUser, data, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("[UserSessionService] cacheUserSession()]:: ", e);
        }
    }

    public void cacheToken(String username, String channelType, String token) {
        try {
            redisService.set(
                sessionStore.getKeyToken(username, channelType),
                FwUtils.hexString(token),
                this.frameworkProperties.getSecurity().getValidDurationInSeconds(),
                TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.error("[UserSessionService] cacheToken()]:: ", e);
        }
    }

    public String getOldToken(String username, String channelType) {
        String keyToken = sessionStore.getKeyToken(username, channelType);
        return redisService.get(keyToken, String.class);
    }

    public void removeOldSessionByChanelType(String username, String channelType) {
        try {
            String keyToken = redisService.get(sessionStore.getKeyToken(username, channelType), String.class);
            if (StringUtils.isNotBlank(keyToken)) {
                redisService.evict(sessionStore.getKeyToken(username, channelType));
            }
            var keyUser = sessionStore.getKeyUser(username, channelType);
            if (StringUtils.isNotBlank(keyUser)) {
                redisService.evict(sessionStore.getKeyUser(username, channelType));
            }
        } catch (Exception e) {
            log.error("[UserSessionService] removeOldSessionByChanelType()]:: ", e);
        }
    }

    public String isUserOnline(String username, String channelType) {
        try {
            return redisService.get(sessionStore.getKeyToken(username, channelType), String.class);
        } catch (Exception var4) {
            return null;
        }
    }

    public UserSession getUserSessionInfo(String username, String channelType) {
        String data = redisService.get(sessionStore.getKeyUser(username, channelType), String.class);
        UserSession sessionInfo = JsonF.jsonToObject(data, UserSession.class);
        if (ObjectUtils.isEmpty(sessionInfo)) {
            throw new FwException(CommonErrorMessage.USER_SESSION_NOT_FOUND);
        } else {
            return sessionInfo;
        }
    }
}
