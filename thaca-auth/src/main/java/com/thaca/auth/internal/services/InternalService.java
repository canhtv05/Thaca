package com.thaca.auth.internal.services;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.domains.User;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.repositories.UserRepository;
import com.thaca.auth.services.KafkaProducerService;
import com.thaca.common.dtos.internal.VerifyEmailTokenDTO;
import com.thaca.framework.blocking.starter.configs.cache.RedisCacheService;
import com.thaca.framework.blocking.starter.services.SessionStore;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalService {

    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;
    private final RedisCacheService redisService;
    private final SessionStore sessionStore;

    @FwMode(name = ServiceMethod.INTERNAL_ACTIVE_USER, type = ModeType.VALIDATE)
    public void validateActiveUserByUserName(VerifyEmailTokenDTO request) {
        if (request.email().contains("+")) {
            throw new FwException(ErrorMessage.EMAIL_INVALID);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.INTERNAL_ACTIVE_USER, type = ModeType.HANDLE)
    public VerifyEmailTokenDTO activeUserByUserName(VerifyEmailTokenDTO request) {
        User user = userRepository
            .findByUsername(request.username())
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        user.setIsActivated(true);
        userRepository.save(user);

        //        kafkaProducerService.sendAndWait(
        //            EventConstants.USER_CREATED_TOPIC,
        //            user.getUsername(),
        //            new UserCreationEvent(user.getUsername(), request.fullname())
        //        );

        return new VerifyEmailTokenDTO(
            user.getUsername(),
            request.fullname(),
            request.email(),
            request.expiredAt(),
            request.jti()
        );
    }
}
