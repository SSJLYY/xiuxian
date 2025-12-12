package com.xiuxian.game.validation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for data consistency validation
 * Allows customization of validation behavior
 */
@Configuration
@ConfigurationProperties(prefix = "xiuxian.validation")
public class ValidationConfig {
    
    /**
     * Whether to enable startup validation
     */
    private boolean enableStartupValidation = true;
    
    /**
     * Whether to fail application startup on critical type mismatches
     */
    private boolean failOnCriticalMismatches = true;
    
    /**
     * Whether to log detailed validation results
     */
    private boolean enableDetailedLogging = true;
    
    /**
     * Maximum number of validation errors to log per entity
     */
    private int maxErrorsPerEntity = 10;
    
    /**
     * Whether to validate DTOs in addition to entities
     */
    private boolean validateDtos = true;
    
    public boolean isEnableStartupValidation() {
        return enableStartupValidation;
    }
    
    public void setEnableStartupValidation(boolean enableStartupValidation) {
        this.enableStartupValidation = enableStartupValidation;
    }
    
    public boolean isFailOnCriticalMismatches() {
        return failOnCriticalMismatches;
    }
    
    public void setFailOnCriticalMismatches(boolean failOnCriticalMismatches) {
        this.failOnCriticalMismatches = failOnCriticalMismatches;
    }
    
    public boolean isEnableDetailedLogging() {
        return enableDetailedLogging;
    }
    
    public void setEnableDetailedLogging(boolean enableDetailedLogging) {
        this.enableDetailedLogging = enableDetailedLogging;
    }
    
    public int getMaxErrorsPerEntity() {
        return maxErrorsPerEntity;
    }
    
    public void setMaxErrorsPerEntity(int maxErrorsPerEntity) {
        this.maxErrorsPerEntity = maxErrorsPerEntity;
    }
    
    public boolean isValidateDtos() {
        return validateDtos;
    }
    
    public void setValidateDtos(boolean validateDtos) {
        this.validateDtos = validateDtos;
    }
}