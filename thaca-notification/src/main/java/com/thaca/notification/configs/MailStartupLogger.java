package com.thaca.notification.configs;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MailStartupLogger implements ApplicationRunner {

    @Value("${spring.mail.host:}")
    private String host;

    @Value("${spring.mail.port:0}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        if (username == null || username.isBlank()) {
            log.error(
                "[MailConfig] MAIL_USERNAME is empty. Check thaca-notification/.env and IntelliJ working directory (should load .env)."
            );
            return;
        }
        log.info("[MailConfig] SMTP ready: host={}, port={}, username={}", host, port, username);
    }
}
