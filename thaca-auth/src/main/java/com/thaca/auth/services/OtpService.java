package com.thaca.auth.services;

import com.thaca.auth.repositories.SystemUserRepository;
import com.thaca.common.events.SendOtpEvent;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final ApplicationEventPublisher eventPublisher;
    private final SystemUserRepository systemUserRepository;

    @Transactional
    public void sendOtp(SendOtpEvent event) {
        systemUserRepository
            .findById(Long.valueOf(event.objectId()))
            .ifPresent(user -> {
                String otp = (int) (Math.random() * 900000 + 100000) + "";
                SendOtpEvent newEvent = SendOtpEvent.builder()
                    .objectId(user.getId().toString())
                    .email(user.getEmail())
                    .otpCode(otp)
                    .timestamp(Instant.now())
                    .metadata(Map.of("useDefaultConfig", true))
                    .build();
                eventPublisher.publishEvent(newEvent);
            });
    }
}
