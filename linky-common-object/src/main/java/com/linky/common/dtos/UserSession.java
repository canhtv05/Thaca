package com.linky.common.dtos;

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

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthInfo {
        private String channel;
        private String username;
    }
}
