package com.thaca.common.dtos.internal.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginReq {

    private String username;
    private String password;
    private String captcha;
    private String captchaId;
}
