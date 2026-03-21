package com.thaca.auth.dtos;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.thaca.auth.domains.Role;
import com.thaca.auth.domains.User;
import com.thaca.framework.core.utils.JsonF;
import com.thaca.framework.core.utils.json.LowerCaseTrimDeserializer;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ImportUserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonDeserialize(using = LowerCaseTrimDeserializer.class)
    private String username;

    @JsonDeserialize(using = LowerCaseTrimDeserializer.class)
    private String password;

    private String role;

    public static ImportUserDTO fromExcelData(Map<String, String> item) {
        ImportUserDTO dto = JsonF.jsonToObject(JsonF.toJson(item), ImportUserDTO.class);
        return dto;
    }

    public User fromEntity(String password, Set<Role> roles) {
        return User.builder()
            .username(username.toLowerCase())
            .password(password)
            .roles(roles)
            .activated(true)
            .isGlobal(false)
            .build();
    }
}
