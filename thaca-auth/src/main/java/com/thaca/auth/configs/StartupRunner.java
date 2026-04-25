package com.thaca.auth.configs;

import com.thaca.auth.services.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupRunner implements CommandLineRunner {

    private final RolePermissionService rolePermissionService;

    @NullMarked
    @Override
    public void run(String... args) {
        rolePermissionService.syncAllToRedis();
    }
}
