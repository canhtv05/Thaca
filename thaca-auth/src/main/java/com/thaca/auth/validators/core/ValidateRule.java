package com.thaca.auth.validators.core;

@FunctionalInterface
public interface ValidateRule<T> {
    void validate(T input);
}
