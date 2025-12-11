package com.backend.server.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContactPreferenceValidator implements ConstraintValidator<ValidContactPreference, String> {
    
    @Override
    public void initialize(ValidContactPreference constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return value.equals("public") || value.equals("private");
    }
}

