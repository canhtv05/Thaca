package com.thaca.common.dtos.events;

public record VerificationEmailEvent(String to, String username, String fullName) {
}
