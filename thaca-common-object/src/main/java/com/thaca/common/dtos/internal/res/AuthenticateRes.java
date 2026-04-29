package com.thaca.common.dtos.internal.res;

import com.thaca.common.dtos.internal.SystemUserDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticateRes {

    private Boolean isAuthenticate;
    private SystemUserDTO info;
}
