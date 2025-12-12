package com.xiuxian.game.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive report containing type analysis results for multiple entities
 * Provides aggregate statistics and summary information
 */
public class TypeAnalysisReport {
    
    private List<FieldTypeAnalysisResult> analysisResults;
    private TypeAnalysisSummary summary;
    
    public TypeAnalysisReport() {
        this.analysisResults = new ArrayList<>();
        this.summary = new TypeAnalysisSummary();
    }
    
    // Methods to add data
    public void addAnalysisResult(FieldTypeAnalysisResult result) {
        analysisResults.add(result);
        updateSummary(result);
    }
    
    // Getters
    public List<FieldTypeAnalysisResult> getAnalysisResults() {
        return analysisResults;
    }
    
    public TypeAnalysisSummary getSummary() {
        return summary;
    }
    
    // Analysis methods
    public List<FieldTypeAnalysisResult> getResultsWithIssues() {
        return analysisResults.stream().filter(FieldTypeAnalysisResult::hasAnyIssues).collect(java.util.stream.Collectors.toList());
    }
    
    public List<FieldTypeAnalysisResult> getResultsWithCriticalIssues() {
        return analysisResults.stream().filter(FieldTypeAnalysisResult::hasCriticalIssues).collect(java.util.stream.Collectors.toList());
    }
    
    public List<FieldTypeAnalysis> getAllIncompatibleFields() {
        List<FieldTypeAnalysis> incompatibleFields = new ArrayList<>();
        for (FieldTypeAnalysisResult result : analysisResults) {
            incompatibleFields.addAll(result.getIncompatibleFieldAnalyses());
        }
        return incompatibleFields;
    }
    
    public List<FieldTypeAnalysis> getAllHighRiskFields() {
        List<FieldTypeAnalysis> highRiskFields = new ArrayList<>();
        for (FieldTypeAnalysisResult result : analysisResults) {
            highRiskFields.addAll(result.getHighRiskFieldAnalyses());
        }
        return highRiskFields;
    }
    
    public boolean hasAnyIssues() {
        return summary.getTotalIncompatibleFields() > 0 || summary.getTotalNonStandardizedFields() > 0;
    }
    
    public boolean hasCriticalIssues() {
        return !getAllHighRiskFields().isEmpty();
    }
    
    public double getOverallCompatibilityPercentage() {
        return summary.getTotalFields() > 0 ? 
                (double) summary.getTotalCompatibleFields() / summary.getTotalFields() * 100 : 100;
    }
    
    public double getOverallStandardizationPercentage() {
        return summary.getTotalFields() > 0 ? 
                (double) summary.getTotalStandardizedFields() / summary.getTotalFields() * 100 : 100;
    }
    
    // Private helper methods
    private void updateSummary(FieldTypeAnalysisResult result) {
        summary.incrementTotalEntities();
        summary.addFields(result.getTotalFields());
        summary.addCompatibleFields(result.getCompatibleFields());
        summary.addStandardizedFields(result.getStandardizedFields());
        
        if (result.hasAnyIssues()) {
            summary.incrementEntitiesWithIssues();
        }
        
        if (result.hasCriticalIssues()) {
            summary.incrementEntitiesWithCriticalIssues();
        }
    }
    
    @Override
    public String toString() {
        return String.format("TypeAnalysisReport{entities=%d, fields=%d, compatible=%.1f%%, standardized=%.1f%%, critical=%s}", 
                summary.getTotalEntities(), summary.getTotalFields(), 
                getOverallCompatibilityPercentage(), getOverallStandardizationPercentage(), hasCriticalIssues());
    }
}