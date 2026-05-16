package com.thaca.common.events.base;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventMetadata {

    private String tenantId;
    private String mailConfigCode;
    private String templateCode;
    private Boolean useDefaultConfig;
    private Map<String, Object> attributes;
}
