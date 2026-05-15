package com.thaca.auth.mappers;

import com.thaca.auth.domains.UserLockHistory;
import com.thaca.auth.dtos.UserLockHistoryDTO;
import com.thaca.framework.core.utils.DateUtils;

public class UserLockHistoryMapper {

    private UserLockHistoryMapper() {}

    public static UserLockHistoryDTO fromEntity(UserLockHistory entity) {
        if (entity == null) {
            return null;
        }
        return UserLockHistoryDTO.builder()
            .id(entity.getId())
            .targetUserId(entity.getTargetUserId())
            .action(entity.getAction())
            .reason(entity.getReason())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .createdAt(DateUtils.dateToString(entity.getCreatedAt()))
            .updatedAt(DateUtils.dateToString(entity.getUpdatedAt()))
            .build();
    }
}
