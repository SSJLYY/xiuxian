package com.xiuxian.game.validation;

/**
 * Represents the analysis result for a single field's type consistency
 * Contains compatibility information and recommendations
 */
public class FieldTypeAnalysis {
    
    private String fieldName;
    private Class<?> actualType;
    private Class<?> expectedType;
    private boolean compatible;
    private boolean standardized;
    private String incompatibilityReason;
    private String suggestion;
    private TypeConversionUtils.ConversionRisk conversionRisk;
    
    // Constructors
    public FieldTypeAnalysis() {}
    
    public FieldTypeAnalysis(String fieldName, Class<?> actualType, Class<?> expectedType, boolean compatible) {
        this.fieldName = fieldName;
        this.actualType = actualType;
        this.expectedType = expectedType;
        this.compatible = compatible;
    }
    
    // Getters and Setters
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public Class<?> getActualType() {
        return actualType;
    }
    
    public void setActualType(Class<?> actualType) {
        this.actualType = actualType;
    }
    
    public Class<?> getExpectedType() {
        return expectedType;
    }
    
    public void setExpectedType(Class<?> expectedType) {
        this.expectedType = expectedType;
    }
    
    public boolean isCompatible() {
        return compatible;
    }
    
    public void setCompatible(boolean compatible) {
        this.compatible = compatible;
    }
    
    public boolean isStandardized() {
        return standardized;
    }
    
    public void setStandardized(boolean standardized) {
        this.standardized = standardized;
    }
    
    public String getIncompatibilityReason() {
        return incompatibilityReason;
    }
    
    public void setIncompatibilityReason(String incompatibilityReason) {
        this.incompatibilityReason = incompatibilityReason;
    }
    
    public String getSuggestion() {
        return suggestion;
    }
    
    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
    
    public TypeConversionUtils.ConversionRisk getConversionRisk() {
        return conversionRisk;
    }
    
    public void setConversionRisk(TypeConversionUtils.ConversionRisk conversionRisk) {
        this.conversionRisk = conversionRisk;
    }
    
    // Utility methods
    public boolean hasIssues() {
        return !compatible || !standardized || conversionRisk == TypeConversionUtils.ConversionRisk.HIGH;
    }
    
    public String getActualTypeName() {
        return actualType != null ? actualType.getSimpleName() : "null";
    }
    
    public String getExpectedTypeName() {
        return expectedType != null ? expectedType.getSimpleName() : "null";
    }
    
    @Override
    public String toString() {
        return String.format("FieldTypeAnalysis{field='%s', %s -> %s, compatible=%s, standardized=%s, risk=%s}", 
                fieldName, getActualTypeName(), getExpectedTypeName(), compatible, standardized, conversionRisk);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        FieldTypeAnalysis that = (FieldTypeAnalysis) o;
        
        return fieldName.equals(that.fieldName);
    }
    
    @Override
    public int hashCode() {
        return fieldName.hashCode();
    }
}