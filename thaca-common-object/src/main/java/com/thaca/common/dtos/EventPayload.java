package com.thaca.common.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventPayload<T> {

    private T payload;
    private String eventType;
    private String eventId;
}
