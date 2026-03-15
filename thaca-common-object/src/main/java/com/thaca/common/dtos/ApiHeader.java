package com.thaca.common.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiHeader {

    private String username;
    private String location;
    private String transId;
    private String traceId;
    private String channel;
    private String language;

    @Builder.Default
    private Long timestamp = System.currentTimeMillis();
}
