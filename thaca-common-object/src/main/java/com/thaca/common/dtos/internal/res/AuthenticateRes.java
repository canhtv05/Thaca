package com.thaca.common.dtos.internal.res;

import com.thaca.common.dtos.internal.AuthUserDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticateRes {

    private Boolean isAuthenticate;
    private AuthUserDTO info;
}
