package com.thaca.notification.dtos.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestConnectionReq {

    private String host;
    private Integer port;
    private String username;
    private String password;
    private Boolean isAuth;
    private Boolean isStarttls;
}
