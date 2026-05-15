package com.thaca.notification.services;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailInlineResourceService {

    private static final String STATIC_PREFIX = "static/";

    public void attachOtpEmailAssets(MimeMessageHelper helper) throws MessagingException {
        addInlineImage(helper, "logo", "assets/images/logo.png");
        addInlineImage(helper, "shield", "assets/images/email/shield.png");
        addInlineImage(helper, "clock", "assets/images/email/clock.png");
    }

    private void addInlineImage(MimeMessageHelper helper, String cid, String classpathRelativePath)
        throws MessagingException {
        ClassPathResource resource = new ClassPathResource(STATIC_PREFIX + classpathRelativePath);
        if (!resource.exists()) {
            log.warn("[EmailInlineResource] Missing classpath asset: {}", classpathRelativePath);
            return;
        }
        helper.addInline(cid, resource);
    }
}
