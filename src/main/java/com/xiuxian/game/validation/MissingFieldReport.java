package com.xiuxian.game.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report containing missing field analysis results
 * Aggregates missing fields across different layer comparisons
 */
public class MissingFieldReport {
    
    private Map<String, List<MissingField>> entityDatabaseMissingFields;
    private Map<String, List<MissingField>> dtoEntityMissingFields;
    private MissingFieldSummary summary;
    
    public MissingFieldReport() {
        this.entityDatabaseMissingFields = new HashMap<>();
        this.dtoEntityMissingFields = new HashMap<>();
        this.summary = new MissingFieldSummary();
    }
    
    // Methods to add data
    public void addEntityDatabaseMissingFields(String entityName, List<MissingField> missingFields) {
        entityDatabaseMissingFields.put(entityName, missingFields);
        updateSummary(missingFields);
    }
    
    public void addDTOEntityMissingFields(String dtoName, List<MissingField> missingFields) {
        dtoEntityMissingFields.put(dtoName, missingFields);
        updateSummary(missingFields);
    }
    
    // Getters
    public Map<String, List<MissingField>> getEntityDatabaseMissingFields() {
        return entityDatabaseMissingFields;
    }
    
    public Map<String, List<MissingField>> getDtoEntityMissingFields() {
        return dtoEntityMissingFields;
    }
    
    public MissingFieldSummary getSummary() {
        return summary;
    }
    
    // Analysis methods
    public List<MissingField> getAllMissingFields() {
        List<MissingField> allMissingFields = new ArrayList<>();
        
        for (List<MissingField> missingFields : entityDatabaseMissingFields.values()) {
            allMissingFields.addAll(missingFields);
        }
        
        for (List<MissingField> missingFields : dtoEntityMissingFields.values()) {
            allMissingFields.addAll(missingFields);
        }
        
        return allMissingFields;
    }
    
    public List<MissingField> getCriticalMissingFields() {
        List<MissingField> criticalFields = new ArrayList<>();
        
        for (MissingField missingField : getAllMissingFields()) {
            // Consider fields missing from database as critical
            if ("database".equals(missingField.getMissingFrom())) {
                criticalFields.add(missingField);
            }
        }
        
        return criticalFields;
    }
    
    public boolean hasAnyMissingFields() {
        return summary.getTotalMissingFields() > 0;
    }
    
    public boolean hasCriticalMissingFields() {
        return !getCriticalMissingFields().isEmpty();
    }
    
    public int getTotalEntitiesAnalyzed() {
        return entityDatabaseMissingFields.size();
    }
    
    public int getTotalDTOsAnalyzed() {
        return dtoEntityMissingFields.size();
    }
    
    // Private helper methods
    private void updateSummary(List<MissingField> missingFields) {
        for (MissingField missingField : missingFields) {
            summary.incrementTotal();
            
            if ("database".equals(missingField.getMissingFrom())) {
                summary.incrementMissingFromDatabase();
            } else if ("entity".equals(missingField.getMissingFrom())) {
                summary.incrementMissingFromEntity();
            } else if ("dto".equals(missingField.getMissingFrom())) {
                summary.incrementMissingFromDTO();
            }
        }
    }
    
    @Override
    public String toString() {
        return String.format("MissingFieldReport{entities=%d, dtos=%d, totalMissing=%d, critical=%s}", 
                getTotalEntitiesAnalyzed(), getTotalDTOsAnalyzed(), 
                summary.getTotalMissingFields(), hasCriticalMissingFields());
    }
}