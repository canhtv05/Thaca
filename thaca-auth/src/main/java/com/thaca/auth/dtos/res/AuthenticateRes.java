package com.thaca.auth.dtos.res;

import com.thaca.auth.dtos.SystemUserDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticateRes {

    private Boolean isAuthenticate;
    private SystemUserDTO info;
    private String accessToken;
}
