package com.xiuxian.game.validation;

/**
 * Summary statistics for type standardization analysis
 * Provides aggregate counts and metrics
 */
public class TypeStandardizationSummary {
    
    private int totalEntities;
    private int validEntities;
    private int invalidEntities;
    private int entitiesWithWarnings;
    
    private int totalMismatches;
    private int criticalMismatches;
    private int warningMismatches;
    private int infoMismatches;
    
    private int totalRecommendations;
    
    public TypeStandardizationSummary() {
        // Initialize all counters to 0
    }
    
    // Increment methods
    public void incrementTotalEntities() {
        totalEntities++;
    }
    
    public void incrementValidEntities() {
        validEntities++;
    }
    
    public void incrementInvalidEntities() {
        invalidEntities++;
    }
    
    public void incrementEntitiesWithWarnings() {
        entitiesWithWarnings++;
    }
    
    public void incrementTotalMismatches() {
        totalMismatches++;
    }
    
    public void incrementCriticalMismatches() {
        criticalMismatches++;
    }
    
    public void incrementWarningMismatches() {
        warningMismatches++;
    }
    
    public void incrementInfoMismatches() {
        infoMismatches++;
    }
    
    public void addRecommendations(int count) {
        totalRecommendations += count;
    }
    
    // Getters
    public int getTotalEntities() {
        return totalEntities;
    }
    
    public int getValidEntities() {
        return validEntities;
    }
    
    public int getInvalidEntities() {
        return invalidEntities;
    }
    
    public int getEntitiesWithWarnings() {
        return entitiesWithWarnings;
    }
    
    public int getTotalMismatches() {
        return totalMismatches;
    }
    
    public int getCriticalMismatches() {
        return criticalMismatches;
    }
    
    public int getWarningMismatches() {
        return warningMismatches;
    }
    
    public int getInfoMismatches() {
        return infoMismatches;
    }
    
    public int getTotalRecommendations() {
        return totalRecommendations;
    }
    
    // Calculated properties
    public double getValidEntityPercentage() {
        return totalEntities > 0 ? (double) validEntities / totalEntities * 100 : 0;
    }
    
    public double getCriticalMismatchPercentage() {
        return totalMismatches > 0 ? (double) criticalMismatches / totalMismatches * 100 : 0;
    }
    
    public boolean isHealthy() {
        return criticalMismatches == 0 && invalidEntities == 0;
    }
    
    public boolean hasIssues() {
        return totalMismatches > 0 || totalRecommendations > 0;
    }
    
    public boolean hasCriticalIssues() {
        return criticalMismatches > 0;
    }
    
    @Override
    public String toString() {
        return String.format("TypeStandardizationSummary{" +
                "entities=%d (valid=%d, invalid=%d, warnings=%d), " +
                "mismatches=%d (critical=%d, warning=%d, info=%d), " +
                "recommendations=%d, healthy=%s}", 
                totalEntities, validEntities, invalidEntities, entitiesWithWarnings,
                totalMismatches, criticalMismatches, warningMismatches, infoMismatches,
                totalRecommendations, isHealthy());
    }
}