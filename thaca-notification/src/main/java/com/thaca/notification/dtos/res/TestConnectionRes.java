package com.thaca.notification.dtos.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestConnectionRes {

    private Boolean success;
    private String message;

    public TestConnectionRes(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
