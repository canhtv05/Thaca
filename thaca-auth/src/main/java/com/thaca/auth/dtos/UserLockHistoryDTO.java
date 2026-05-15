package com.thaca.auth.dtos;

import com.thaca.common.dtos.BaseAuditResponse;
import com.thaca.common.enums.AccountStatus;
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
public class UserLockHistoryDTO extends BaseAuditResponse {

    private Long id;
    private Long targetUserId;
    private AccountStatus action;
    private String reason;
}
