package com.thaca.common.dtos.internal;

import java.time.Instant;

public record VerifyEmailTokenDTO(String username, String fullname, String email, Instant expiredAt, String jti) {}
