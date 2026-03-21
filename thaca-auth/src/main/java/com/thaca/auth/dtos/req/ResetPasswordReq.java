package com.thaca.auth.dtos.req;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ResetPasswordReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String email;

    private String newPassword;

    @JsonProperty("OTP")
    private String OTP;
}
