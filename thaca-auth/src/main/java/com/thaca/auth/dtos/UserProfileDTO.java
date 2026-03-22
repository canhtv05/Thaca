package com.thaca.auth.dtos;

import com.thaca.auth.domains.Role;
import com.thaca.auth.domains.User;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    private String username;
    private String email;
    private Boolean isGlobal;
    private List<String> roles;
    private List<String> roleLabels;
    private List<String> permissions;
    private String secretKey;
    private String channel;

    public static UserProfileDTO fromEntity(User user) {
        return UserProfileDTO.builder()
            .username(user.getUsername())
            .isGlobal(user.getIsGlobal())
            .roles(user.getRoles().stream().map(Role::getCode).collect(Collectors.toList()))
            .roleLabels(user.getRoles().stream().map(Role::getDescription).collect(Collectors.toList()))
            .email(user.getEmail())
            .build();
    }
}
