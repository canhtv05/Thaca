package com.thaca.common.socket;

import com.thaca.common.enums.WsType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WsMessage {

    private WsType type;
    private String topic;
    private String userId;
    private String messageVi;
    private String messageEn;
    private String transId;
    private String data;

    public static WsMessage error(String messageVi, String messageEn) {
        return WsMessage.builder().type(WsType.ERROR).messageVi(messageVi).messageEn(messageEn).build();
    }

    public static WsMessage error(String transId, String messageVi, String messageEn) {
        return WsMessage.builder()
            .type(WsType.ERROR)
            .transId(transId)
            .messageVi(messageVi)
            .messageEn(messageEn)
            .build();
    }

    public static WsMessage of(WsType type, String topic, String data) {
        return WsMessage.builder().type(type).topic(topic).data(data).build();
    }
}
