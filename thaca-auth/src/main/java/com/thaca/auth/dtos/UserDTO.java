package com.thaca.auth.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thaca.auth.domains.Role;
import com.thaca.auth.domains.User;
import com.thaca.framework.core.utils.json.InstantToStringSerializer;
import com.thaca.framework.core.utils.json.LowerCaseTrimDeserializer;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

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

    @JsonDeserialize(using = LowerCaseTrimDeserializer.class)
    private String password;

    @JsonDeserialize(using = LowerCaseTrimDeserializer.class)
    private String email;

    private Boolean isActivated;
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
            .isActivated(user.getIsActivated())
            .roles(user.getRoles().stream().map(Role::getCode).collect(Collectors.toList()))
            .roleLabels(user.getRoles().stream().map(Role::getDescription).collect(Collectors.toList()))
            .build();
    }
}
