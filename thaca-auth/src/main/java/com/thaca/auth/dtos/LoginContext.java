package com.thaca.auth.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginContext {

    private String username;
    private String ip;
    private String userAgent;
}
