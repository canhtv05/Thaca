package com.thaca.framework.blocking.starter.configs;

import com.thaca.framework.core.config.FrameworkProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(FrameworkProperties.class)
public class RedisConfig {

    private final FrameworkProperties frameworkProperties;

    @Bean
    Config config() {
        Config config = new Config();

        String password = frameworkProperties.getRedis().getPassword();
        String address = frameworkProperties.getRedis().getAddress();
        if (StringUtils.isBlank(address)) {
            address = "redis://localhost:6379";
        }
        if (StringUtils.isNotBlank(password)) {
            String enCodePassword = URLEncoder.encode(password, StandardCharsets.UTF_8);
            String rawAddress = address.replace("redis://", "").replace("rediss://", "");
            address = "redis://" + enCodePassword + "@" + rawAddress;
        }
        if (!address.startsWith("redis://") && !address.startsWith("rediss://")) address = "redis://" + address;

        var single = config
            .useSingleServer()
            .setAddress(address)
            .setConnectionPoolSize(frameworkProperties.getRedis().getMaxPoolSize())
            .setTimeout(10000)
            .setConnectTimeout(10000)
            .setConnectionMinimumIdleSize(frameworkProperties.getRedis().getMinimumIdle());

        if (StringUtils.isNotBlank(frameworkProperties.getRedis().getClientName())) single.setClientName(
            frameworkProperties.getRedis().getClientName()
        );
        return config;
    }

    @Bean
    RedissonClient redissonClient(Config config) {
        return Redisson.create(config);
    }
}
