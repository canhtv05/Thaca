package com.thaca.auth.dtos.res;

import com.thaca.auth.dtos.AuthUserDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticateRes {

    private boolean authenticate;
    private AuthUserDTO info;
}
