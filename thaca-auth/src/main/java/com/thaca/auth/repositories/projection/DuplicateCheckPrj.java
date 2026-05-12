package com.thaca.auth.repositories.projection;

public interface DuplicateCheckPrj {
    Long getTenantId();

    String getUsername();

    String getEmail();
}
