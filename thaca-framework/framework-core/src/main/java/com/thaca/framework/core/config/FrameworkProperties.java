package com.thaca.framework.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application", ignoreInvalidFields = true)
public class FrameworkProperties {

    private SecurityConfig security = new SecurityConfig();
    private RedisConfig redis = new RedisConfig();

    @Getter
    @Setter
    public static class SecurityConfig{
        private String base64Secret;
        private Long validDurationInSeconds;
        private Long refreshDurationInSeconds;
        private String cookieDomain;
    }

    @Getter
    @Setter
    public static class RedisConfig {
        private String clientName;
        private String address;
        private String password;
        private Integer minimumIdle;
        private Integer maxPoolSize;
    }
}
