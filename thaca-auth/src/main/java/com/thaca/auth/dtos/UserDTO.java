package com.thaca.auth.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.thaca.auth.domains.Role;
import com.thaca.auth.domains.User;
import com.thaca.framework.core.utils.json.InstantToStringSerializer;
import com.thaca.framework.core.utils.json.LowerCaseTrimDeserializer;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String fullname;

    @JsonDeserialize(using = LowerCaseTrimDeserializer.class)
    private String username;

    @JsonDeserialize(using = LowerCaseTrimDeserializer.class)
    private String password;

    @JsonDeserialize(using = LowerCaseTrimDeserializer.class)
    private String email;

    private boolean activated;
    private Boolean isGlobal;
    private List<String> roles;
    private List<String> roleLabels;

    @JsonSerialize(using = InstantToStringSerializer.class)
    private Instant createdDate;

    @JsonSerialize(using = InstantToStringSerializer.class)
    private Instant modifiedDate;

    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .password(null)
            .email(user.getEmail())
            .activated(user.isActivated())
            .isGlobal(user.getIsGlobal())
            .roles(user.getRoles().stream().map(Role::getCode).collect(Collectors.toList()))
            .roleLabels(user.getRoles().stream().map(Role::getDescription).collect(Collectors.toList()))
            .build();
    }
}
