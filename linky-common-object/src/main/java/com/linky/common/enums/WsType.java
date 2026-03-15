package com.linky.common.enums;

public enum WsType {
    PING,
    PONG,
    SUBSCRIBE,
    UNSUBSCRIBE,
    KICK,
    ERROR,
    MESSAGE,
    RESPONSE_API,
    FRIEND_REQUEST,
    USER_ONLINE, // user vừa connect
    USER_OFFLINE, // user vừa disconnect
    CHECK_USER_ONLINE // client -> server
}
