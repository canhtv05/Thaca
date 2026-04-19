package com.thaca.auth.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thaca.auth.domains.User;
import com.thaca.framework.core.utils.json.LowerCaseTrimDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import tools.jackson.databind.annotation.JsonDeserialize;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private Long id;
    private String fullname;

    @JsonDeserialize(using = LowerCaseTrimDeserializer.class)
    private String username;

    @JsonIgnore
    @JsonDeserialize(using = LowerCaseTrimDeserializer.class)
    private String password;

    @JsonDeserialize(using = LowerCaseTrimDeserializer.class)
    private String email;

    private Boolean isActivated;
    private Boolean isLocked;

    public static UserDTO fromEntity(User user) {
        UserDTO userDTO = new UserDTO();
        if (user == null) {
            return null;
        }
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }
}
