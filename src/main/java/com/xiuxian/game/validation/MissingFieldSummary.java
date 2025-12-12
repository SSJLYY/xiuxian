package com.xiuxian.game.validation;

/**
 * Summary statistics for missing field analysis
 * Provides aggregate counts and metrics
 */
public class MissingFieldSummary {
    
    private int totalMissingFields;
    private int missingFromDatabase;
    private int missingFromEntity;
    private int missingFromDTO;
    
    public MissingFieldSummary() {
        // Initialize all counters to 0
    }
    
    // Increment methods
    public void incrementTotal() {
        totalMissingFields++;
    }
    
    public void incrementMissingFromDatabase() {
        missingFromDatabase++;
    }
    
    public void incrementMissingFromEntity() {
        missingFromEntity++;
    }
    
    public void incrementMissingFromDTO() {
        missingFromDTO++;
    }
    
    // Getters
    public int getTotalMissingFields() {
        return totalMissingFields;
    }
    
    public int getMissingFromDatabase() {
        return missingFromDatabase;
    }
    
    public int getMissingFromEntity() {
        return missingFromEntity;
    }
    
    public int getMissingFromDTO() {
        return missingFromDTO;
    }
    
    // Calculated properties
    public double getDatabaseMissingPercentage() {
        return totalMissingFields > 0 ? (double) missingFromDatabase / totalMissingFields * 100 : 0;
    }
    
    public double getEntityMissingPercentage() {
        return totalMissingFields > 0 ? (double) missingFromEntity / totalMissingFields * 100 : 0;
    }
    
    public double getDTOMissingPercentage() {
        return totalMissingFields > 0 ? (double) missingFromDTO / totalMissingFields * 100 : 0;
    }
    
    public boolean hasAnyMissingFields() {
        return totalMissingFields > 0;
    }
    
    public boolean hasCriticalMissingFields() {
        return missingFromDatabase > 0; // Database missing fields are considered critical
    }
    
    @Override
    public String toString() {
        return String.format("MissingFieldSummary{total=%d, database=%d, entity=%d, dto=%d}", 
                totalMissingFields, missingFromDatabase, missingFromEntity, missingFromDTO);
    }
}