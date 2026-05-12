package com.thaca.framework.core.validations;

import java.util.List;

public class Validator<T> {

    private final List<ValidateRule<T>> rules;

    public Validator(List<ValidateRule<T>> rules) {
        this.rules = rules;
    }

    public void validate(T t) {
        for (ValidateRule<T> rule : rules) {
            rule.validate(t);
        }
    }

    public void add(ValidateRule<T> rule) {
        rules.add(rule);
    }
}
