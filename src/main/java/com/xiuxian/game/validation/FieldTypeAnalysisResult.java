package com.xiuxian.game.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the results of field type analysis for an entity or DTO
 * Aggregates individual field analyses and provides summary information
 */
public class FieldTypeAnalysisResult {
    
    private String entityName;
    private String analysisType;
    private List<FieldTypeAnalysis> fieldAnalyses;
    private List<String> errors;
    
    public FieldTypeAnalysisResult() {
        this.fieldAnalyses = new ArrayList<>();
        this.errors = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getEntityName() {
        return entityName;
    }
    
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    
    public String getAnalysisType() {
        return analysisType;
    }
    
    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }
    
    public List<FieldTypeAnalysis> getFieldAnalyses() {
        return fieldAnalyses;
    }
    
    public void setFieldAnalyses(List<FieldTypeAnalysis> fieldAnalyses) {
        this.fieldAnalyses = fieldAnalyses;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    // Methods to add data
    public void addFieldAnalysis(FieldTypeAnalysis fieldAnalysis) {
        fieldAnalyses.add(fieldAnalysis);
    }
    
    public void addError(String error) {
        errors.add(error);
    }
    
    // Analysis methods
    public int getTotalFields() {
        return fieldAnalyses.size();
    }
    
    public int getCompatibleFields() {
        return (int) fieldAnalyses.stream().filter(FieldTypeAnalysis::isCompatible).count();
    }
    
    public int getIncompatibleFields() {
        return getTotalFields() - getCompatibleFields();
    }
    
    public int getStandardizedFields() {
        return (int) fieldAnalyses.stream().filter(FieldTypeAnalysis::isStandardized).count();
    }
    
    public int getNonStandardizedFields() {
        return getTotalFields() - getStandardizedFields();
    }
    
    public List<FieldTypeAnalysis> getIncompatibleFieldAnalyses() {
        return fieldAnalyses.stream().filter(analysis -> !analysis.isCompatible()).collect(java.util.stream.Collectors.toList());
    }
    
    public List<FieldTypeAnalysis> getHighRiskFieldAnalyses() {
        return fieldAnalyses.stream()
                .filter(analysis -> analysis.getConversionRisk() == TypeConversionUtils.ConversionRisk.HIGH)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public boolean hasAnyIssues() {
        return getIncompatibleFields() > 0 || getNonStandardizedFields() > 0 || !errors.isEmpty();
    }
    
    public boolean hasCriticalIssues() {
        return !getHighRiskFieldAnalyses().isEmpty() || !errors.isEmpty();
    }
    
    public double getCompatibilityPercentage() {
        return getTotalFields() > 0 ? (double) getCompatibleFields() / getTotalFields() * 100 : 100;
    }
    
    public double getStandardizationPercentage() {
        return getTotalFields() > 0 ? (double) getStandardizedFields() / getTotalFields() * 100 : 100;
    }
    
    @Override
    public String toString() {
        return String.format("FieldTypeAnalysisResult{entity='%s', type='%s', fields=%d, compatible=%d, standardized=%d, errors=%d}", 
                entityName, analysisType, getTotalFields(), getCompatibleFields(), getStandardizedFields(), errors.size());
    }
}