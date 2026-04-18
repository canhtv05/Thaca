package com.thaca.auth.configs;

import com.thaca.auth.services.RolePermissionMappingService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupRunner implements CommandLineRunner {

    private final RolePermissionMappingService rolePermissionMappingService;

    @NullMarked
    @Override
    public void run(String... args) {
        rolePermissionMappingService.syncAllToRedis();
    }
}
