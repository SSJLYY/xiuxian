package com.xiuxian.game.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive report of type standardization analysis
 * Contains validation results, mismatches, and recommendations
 */
public class TypeStandardizationReport {
    
    private List<TypeValidationResult> entityResults;
    private Map<String, Map<String, TypeMismatch>> typeMismatches;
    private Map<String, List<TypeStandardizationRecommendation>> recommendations;
    private TypeStandardizationSummary summary;
    
    public TypeStandardizationReport() {
        this.entityResults = new ArrayList<>();
        this.typeMismatches = new HashMap<>();
        this.recommendations = new HashMap<>();
        this.summary = new TypeStandardizationSummary();
    }
    
    // Methods to add data
    public void addEntityResult(TypeValidationResult result) {
        entityResults.add(result);
        updateSummary(result);
    }
    
    public void addTypeMismatches(String entityName, Map<String, TypeMismatch> mismatches) {
        typeMismatches.put(entityName, mismatches);
    }
    
    public void addRecommendations(String tableName, List<TypeStandardizationRecommendation> tableRecommendations) {
        recommendations.put(tableName, tableRecommendations);
    }
    
    // Getters
    public List<TypeValidationResult> getEntityResults() {
        return entityResults;
    }
    
    public Map<String, Map<String, TypeMismatch>> getTypeMismatches() {
        return typeMismatches;
    }
    
    public Map<String, List<TypeStandardizationRecommendation>> getRecommendations() {
        return recommendations;
    }
    
    public TypeStandardizationSummary getSummary() {
        return summary;
    }
    
    // Analysis methods
    public List<TypeMismatch> getAllCriticalMismatches() {
        List<TypeMismatch> criticalMismatches = new ArrayList<>();
        
        for (TypeValidationResult result : entityResults) {
            for (TypeMismatch mismatch : result.getMismatches()) {
                if (mismatch.getSeverity() == TypeMismatch.Severity.CRITICAL) {
                    criticalMismatches.add(mismatch);
                }
            }
        }
        
        return criticalMismatches;
    }
    
    public List<TypeStandardizationRecommendation> getHighPriorityRecommendations() {
        List<TypeStandardizationRecommendation> highPriority = new ArrayList<>();
        
        for (List<TypeStandardizationRecommendation> tableRecommendations : recommendations.values()) {
            for (TypeStandardizationRecommendation recommendation : tableRecommendations) {
                if (recommendation.getPriority() == TypeStandardizationRecommendation.Priority.HIGH ||
                    recommendation.getPriority() == TypeStandardizationRecommendation.Priority.CRITICAL) {
                    highPriority.add(recommendation);
                }
            }
        }
        
        return highPriority;
    }
    
    public boolean hasAnyIssues() {
        return summary.getTotalMismatches() > 0 || summary.getTotalRecommendations() > 0;
    }
    
    public boolean hasCriticalIssues() {
        return summary.getCriticalMismatches() > 0 || 
               getHighPriorityRecommendations().size() > 0;
    }
    
    // Private helper methods
    private void updateSummary(TypeValidationResult result) {
        summary.incrementTotalEntities();
        
        if (result.isValid()) {
            summary.incrementValidEntities();
        } else {
            summary.incrementInvalidEntities();
        }
        
        if (result.hasWarnings()) {
            summary.incrementEntitiesWithWarnings();
        }
        
        // Count mismatches by severity
        for (TypeMismatch mismatch : result.getMismatches()) {
            summary.incrementTotalMismatches();
            
            switch (mismatch.getSeverity()) {
                case CRITICAL:
                    summary.incrementCriticalMismatches();
                    break;
                case WARNING:
                    summary.incrementWarningMismatches();
                    break;
                case INFO:
                    summary.incrementInfoMismatches();
                    break;
            }
        }
        
        // Count recommendations
        String tableName = result.getTableName();
        if (tableName != null && recommendations.containsKey(tableName)) {
            summary.addRecommendations(recommendations.get(tableName).size());
        }
    }
    
    @Override
    public String toString() {
        return String.format("TypeStandardizationReport{entities=%d, mismatches=%d, recommendations=%d, critical=%s}", 
                summary.getTotalEntities(), summary.getTotalMismatches(), 
                summary.getTotalRecommendations(), hasCriticalIssues());
    }
}