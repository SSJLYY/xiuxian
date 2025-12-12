package com.xiuxian.game.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a type mismatch between layers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeMismatch {
    
    private String fieldName;
    private String expectedType;
    private String actualType;
    private String layer; // "backend-database", "frontend-backend", etc.
    private String entityName;
    private String tableName;
    private String suggestion;
    private Severity severity;
    
    public enum Severity {
        CRITICAL, // Will cause runtime errors
        WARNING,  // May cause issues
        INFO      // Informational only
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s.%s: Expected %s but found %s in %s layer. %s", 
            severity, entityName, fieldName, expectedType, actualType, layer, 
            suggestion != null ? "Suggestion: " + suggestion : "");
    }
}