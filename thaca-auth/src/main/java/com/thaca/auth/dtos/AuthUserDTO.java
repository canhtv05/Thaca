package com.thaca.auth.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthUserDTO {

    private Long id;
    private String username;
    private String email;
    private String fullname;
    private Boolean isActivated;
    private Boolean isLocked;
    private Boolean isSuperAdmin;
    private Set<String> roles;
}
