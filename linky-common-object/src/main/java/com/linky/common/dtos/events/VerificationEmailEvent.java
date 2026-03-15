package com.linky.common.dtos.events;

public record VerificationEmailEvent(String to, String username, String fullName) {
}
