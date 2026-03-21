package com.thaca.auth.dtos.req;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String currentPassword;
    private String newPassword;
    private String channel;
}
