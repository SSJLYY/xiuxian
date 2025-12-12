package com.xiuxian.game.validation;

/**
 * Summary statistics for type analysis across multiple entities
 * Provides aggregate counts and percentages
 */
public class TypeAnalysisSummary {
    
    private int totalEntities;
    private int entitiesWithIssues;
    private int entitiesWithCriticalIssues;
    
    private int totalFields;
    private int totalCompatibleFields;
    private int totalStandardizedFields;
    
    public TypeAnalysisSummary() {
        // Initialize all counters to 0
    }
    
    // Increment methods
    public void incrementTotalEntities() {
        totalEntities++;
    }
    
    public void incrementEntitiesWithIssues() {
        entitiesWithIssues++;
    }
    
    public void incrementEntitiesWithCriticalIssues() {
        entitiesWithCriticalIssues++;
    }
    
    public void addFields(int count) {
        totalFields += count;
    }
    
    public void addCompatibleFields(int count) {
        totalCompatibleFields += count;
    }
    
    public void addStandardizedFields(int count) {
        totalStandardizedFields += count;
    }
    
    // Getters
    public int getTotalEntities() {
        return totalEntities;
    }
    
    public int getEntitiesWithIssues() {
        return entitiesWithIssues;
    }
    
    public int getEntitiesWithCriticalIssues() {
        return entitiesWithCriticalIssues;
    }
    
    public int getTotalFields() {
        return totalFields;
    }
    
    public int getTotalCompatibleFields() {
        return totalCompatibleFields;
    }
    
    public int getTotalIncompatibleFields() {
        return totalFields - totalCompatibleFields;
    }
    
    public int getTotalStandardizedFields() {
        return totalStandardizedFields;
    }
    
    public int getTotalNonStandardizedFields() {
        return totalFields - totalStandardizedFields;
    }
    
    // Calculated properties
    public double getCompatibilityPercentage() {
        return totalFields > 0 ? (double) totalCompatibleFields / totalFields * 100 : 100;
    }
    
    public double getStandardizationPercentage() {
        return totalFields > 0 ? (double) totalStandardizedFields / totalFields * 100 : 100;
    }
    
    public double getEntitiesWithIssuesPercentage() {
        return totalEntities > 0 ? (double) entitiesWithIssues / totalEntities * 100 : 0;
    }
    
    public double getEntitiesWithCriticalIssuesPercentage() {
        return totalEntities > 0 ? (double) entitiesWithCriticalIssues / totalEntities * 100 : 0;
    }
    
    public boolean isHealthy() {
        return entitiesWithCriticalIssues == 0 && getTotalIncompatibleFields() == 0;
    }
    
    public boolean hasAnyIssues() {
        return entitiesWithIssues > 0 || getTotalIncompatibleFields() > 0 || getTotalNonStandardizedFields() > 0;
    }
    
    public boolean hasCriticalIssues() {
        return entitiesWithCriticalIssues > 0;
    }
    
    @Override
    public String toString() {
        return String.format("TypeAnalysisSummary{entities=%d (issues=%d, critical=%d), fields=%d (compatible=%.1f%%, standardized=%.1f%%), healthy=%s}", 
                totalEntities, entitiesWithIssues, entitiesWithCriticalIssues, totalFields, 
                getCompatibilityPercentage(), getStandardizationPercentage(), isHealthy());
    }
}