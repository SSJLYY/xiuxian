package com.xiuxian.game.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Type validation result container
 * Contains validation status and detailed error information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeValidationResult {
    
    @Builder.Default
    private boolean valid = true;
    
    @Builder.Default
    private List<TypeMismatch> mismatches = new ArrayList<>();
    
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    
    private String entityName;
    private String tableName;
    
    public void addMismatch(TypeMismatch mismatch) {
        this.mismatches.add(mismatch);
        this.valid = false;
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    public boolean hasErrors() {
        return !mismatches.isEmpty();
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
}