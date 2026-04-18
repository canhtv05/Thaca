package com.thaca.framework.core.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiHeader {

    private String username;
    private String location;
    private String channel;
    private String apiKey;

    @Builder.Default
    private Long timestamp = System.currentTimeMillis();
}
