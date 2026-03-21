package com.thaca.auth.dtos.req;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogoutReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String channel;
}
