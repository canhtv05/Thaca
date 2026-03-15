package com.thaca.common.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSession {

    private String sessionId;
    private String channel;
    private String username;
    private String secretKey;
}
