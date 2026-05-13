package com.thaca.framework.core.validations;

@FunctionalInterface
public interface ValidateRule<T> {
    void validate(T input);
}
