package com.thaca.auth.kafka;

import com.thaca.common.events.SendOtpEvent;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void sendOtp(String userId, String email, String otp) {
        SendOtpEvent event = SendOtpEvent.builder()
            .objectId(userId)
            .email(email)
            .otpCode(otp)
            .timestamp(Instant.now())
            .build();

        eventPublisher.publishEvent(event);
    }
}
