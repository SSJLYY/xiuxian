package com.xiuxian.game.validation;

/**
 * Represents validation result for a single API response field
 * Contains information about field presence, type correctness, and issues
 */
public class APIFieldValidation {
    
    private String fieldName;
    private Class<?> actualType;
    private Class<?> expectedType;
    private boolean present;
    private boolean valid;
    private boolean hasValue;
    private String issue;
    private String suggestion;
    
    // Constructors
    public APIFieldValidation() {}
    
    public APIFieldValidation(String fieldName, Class<?> actualType, boolean present, boolean valid) {
        this.fieldName = fieldName;
        this.actualType = actualType;
        this.present = present;
        this.valid = valid;
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
    
    public boolean isPresent() {
        return present;
    }
    
    public void setPresent(boolean present) {
        this.present = present;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public boolean isHasValue() {
        return hasValue;
    }
    
    public void setHasValue(boolean hasValue) {
        this.hasValue = hasValue;
    }
    
    public String getIssue() {
        return issue;
    }
    
    public void setIssue(String issue) {
        this.issue = issue;
    }
    
    public String getSuggestion() {
        return suggestion;
    }
    
    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
    
    // Utility methods
    public String getActualTypeName() {
        return actualType != null ? actualType.getSimpleName() : "null";
    }
    
    public String getExpectedTypeName() {
        return expectedType != null ? expectedType.getSimpleName() : "null";
    }
    
    public boolean hasIssues() {
        return !present || !valid || issue != null;
    }
    
    public boolean isCritical() {
        return !present || (issue != null && issue.contains("missing"));
    }
    
    @Override
    public String toString() {
        return String.format("APIFieldValidation{field='%s', type=%s, present=%s, valid=%s, hasValue=%s, issue='%s'}", 
                fieldName, getActualTypeName(), present, valid, hasValue, issue);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        APIFieldValidation that = (APIFieldValidation) o;
        
        return fieldName.equals(that.fieldName);
    }
    
    @Override
    public int hashCode() {
        return fieldName.hashCode();
    }
}