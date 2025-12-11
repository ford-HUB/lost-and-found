package com.backend.server.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class ImageFilesValidator implements ConstraintValidator<ValidImageFiles, List<MultipartFile>> {
    
    private int min;
    private int max;
    
    @Override
    public void initialize(ValidImageFiles constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }
    
    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        if (files == null || files.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("At least one image is required")
                   .addConstraintViolation();
            return false;
        }
        
        long validImageCount = files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .count();
        
        if (validImageCount == 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("At least one valid image is required")
                   .addConstraintViolation();
            return false;
        }
        
        if (validImageCount > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Maximum " + max + " images allowed")
                   .addConstraintViolation();
            return false;
        }
        
        // Validate image types
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("All files must be images")
                           .addConstraintViolation();
                    return false;
                }
            }
        }
        
        return true;
    }
}

