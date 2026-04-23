package com.thaca.auth.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thaca.auth.domains.User;
import com.thaca.common.dtos.BaseAuditResponse;
import com.thaca.framework.core.utils.DateUtils;
import com.thaca.framework.core.utils.json.LowerCaseTrimDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.annotation.JsonDeserialize;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO extends BaseAuditResponse {

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
        return fromEntity(user, false);
    }

    public static UserDTO fromEntity(User user, boolean isCms) {
        if (user == null) {
            return null;
        }
        UserDTO dto = UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .isActivated(user.getIsActivated())
            .isLocked(user.getIsLocked())
            .build();

        if (isCms) {
            dto.setCreatedAt(DateUtils.dateToString(user.getCreatedAt()));
            dto.setUpdatedAt(DateUtils.dateToString(user.getUpdatedAt()));
            dto.setCreatedBy(user.getCreatedBy());
            dto.setUpdatedBy(user.getUpdatedBy());
        }
        return dto;
    }
}
