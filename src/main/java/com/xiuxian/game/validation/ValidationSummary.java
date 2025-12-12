package com.xiuxian.game.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary of validation results
 * Contains aggregate information about validation status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationSummary {
    
    private int totalEntities;
    private int validEntities;
    private int entitiesWithWarnings;
    private int entitiesWithErrors;
    private int criticalMismatches;
    private int warningMismatches;
    
    /**
     * Check if overall validation was successful
     */
    public boolean isSuccessful() {
        return entitiesWithErrors == 0 && criticalMismatches == 0;
    }
    
    /**
     * Check if there are any issues (errors or warnings)
     */
    public boolean hasIssues() {
        return entitiesWithErrors > 0 || entitiesWithWarnings > 0 || 
               criticalMismatches > 0 || warningMismatches > 0;
    }
    
    /**
     * Get success rate as percentage
     */
    public double getSuccessRate() {
        if (totalEntities == 0) {
            return 100.0;
        }
        return (double) validEntities / totalEntities * 100.0;
    }
}