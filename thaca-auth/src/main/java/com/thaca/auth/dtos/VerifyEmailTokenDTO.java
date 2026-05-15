package com.thaca.auth.dtos;

import java.time.Instant;

public record VerifyEmailTokenDTO(String username, String fullname, String email, Instant expiredAt, String jti) {}
