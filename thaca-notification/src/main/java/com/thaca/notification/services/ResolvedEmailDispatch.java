package com.thaca.notification.services;

import org.springframework.mail.javamail.JavaMailSender;

public record ResolvedEmailDispatch(
    JavaMailSender mailSender,
    String senderAddress,
    String templateCode,
    String notificationType,
    String tenantId
) {}
