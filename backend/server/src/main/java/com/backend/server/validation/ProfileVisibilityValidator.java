package com.backend.server.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ProfileVisibilityValidator implements ConstraintValidator<ValidProfileVisibility, String> {
    
    @Override
    public void initialize(ValidProfileVisibility constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return value.equals("public") || value.equals("items-only") || value.equals("private");
    }
}

