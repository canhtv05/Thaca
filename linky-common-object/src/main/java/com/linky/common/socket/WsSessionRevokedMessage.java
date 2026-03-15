package com.linky.common.socket;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WsSessionRevokedMessage {

    private String tokenSessionValid;
    private String tokenSessionCurrent;
    private String channelType;
}
