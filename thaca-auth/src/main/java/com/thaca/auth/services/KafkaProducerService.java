package com.thaca.auth.services;

import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.exceptions.FwException;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T> void sendAndWait(String topic, String key, T payload) {
        try {
            kafkaTemplate.send(topic, key, payload).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FwException(CommonErrorMessage.INTERNAL_SERVER_ERROR);
        } catch (ExecutionException e) {
            throw new FwException(CommonErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
