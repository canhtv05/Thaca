package com.linky.common.dtos.events;

public record ForgotPasswordEvent(String to, String username) {
}
