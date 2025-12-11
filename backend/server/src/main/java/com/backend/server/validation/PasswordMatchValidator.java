package com.backend.server.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {
    
    private String passwordField;
    private String confirmPasswordField;
    
    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        this.passwordField = constraintAnnotation.password();
        this.confirmPasswordField = constraintAnnotation.confirmPassword();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        try {
            Field password = value.getClass().getDeclaredField(passwordField);
            Field confirmPassword = value.getClass().getDeclaredField(confirmPasswordField);
            
            password.setAccessible(true);
            confirmPassword.setAccessible(true);
            
            Object passwordValue = password.get(value);
            Object confirmPasswordValue = confirmPassword.get(value);
            
            if (passwordValue == null && confirmPasswordValue == null) {
                return true;
            }
            
            if (passwordValue == null || confirmPasswordValue == null) {
                return false;
            }
            
            return passwordValue.equals(confirmPasswordValue);
        } catch (Exception e) {
            return false;
        }
    }
}

