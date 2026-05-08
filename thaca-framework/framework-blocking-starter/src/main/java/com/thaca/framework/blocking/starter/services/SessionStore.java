package com.thaca.framework.blocking.starter.services;

import com.thaca.framework.core.enums.ChannelType;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionStore {

    private final Environment environment;

    public String getKeyToken(String username, ChannelType channel) {
        String envRunning = environment.getActiveProfiles()[0];
        return String.format("%s:token:%s:%s", envRunning, username, channel.name());
    }

    public String getKeyUser(String username, String channel) {
        String envRunning = environment.getActiveProfiles()[0];
        return String.format("%s:user:%s:%s", envRunning, username, channel);
    }

    public String getKeyVerification(String userId) {
        String envRunning = environment.getActiveProfiles()[0];
        return String.format("%s:verify:email:%s", envRunning, userId);
    }

    public String getKeyForgotPassword(String token) {
        String envRunning = environment.getActiveProfiles()[0];
        return String.format("%s:forgot:password:%s", envRunning, token);
    }

    public String getKeyCaptcha(String captchaId) {
        String envRunning = environment.getActiveProfiles()[0];
        return String.format("%s:captcha:%s", envRunning, captchaId);
    }
}
