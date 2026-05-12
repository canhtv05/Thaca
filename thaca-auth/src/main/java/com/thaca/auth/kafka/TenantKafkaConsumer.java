package com.thaca.auth.kafka;

import com.thaca.auth.repositories.UserRepository;
import com.thaca.common.constants.EventConstants;
import com.thaca.common.dtos.events.TenantDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantKafkaConsumer {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = EventConstants.TENANT_DELETED_TOPIC, groupId = EventConstants.TENANT_DELETED_GROUP_ID)
    public void consumeTenantDeleted(String message) {
        log.info("Received TenantDeletedEvent raw message: {}", message);
        try {
            TenantDeletedEvent event = objectMapper.readValue(message, TenantDeletedEvent.class);
            log.info("Parsed event for tenantId: {}. Cleaning up user mappings.", event.getTenantId());
            userRepository.deleteUserTenantsByTenantId(event.getTenantId());
            log.info("Successfully cleaned up user_tenants for tenantId: {}", event.getTenantId());
        } catch (Exception e) {
            log.error("Error processing TenantDeletedEvent: {}", message, e);
        }
    }
}
