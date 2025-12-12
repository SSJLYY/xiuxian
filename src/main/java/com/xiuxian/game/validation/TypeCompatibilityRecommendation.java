package com.xiuxian.game.validation;

/**
 * Represents a recommendation for improving type compatibility
 * Contains specific guidance for resolving type mismatches
 */
public class TypeCompatibilityRecommendation {
    
    private String fieldName;
    private Class<?> currentType;
    private Class<?> recommendedType;
    private String reason;
    private Priority priority;
    private String suggestion;
    
    // Constructors
    public TypeCompatibilityRecommendation() {}
    
    private TypeCompatibilityRecommendation(Builder builder) {
        this.fieldName = builder.fieldName;
        this.currentType = builder.currentType;
        this.recommendedType = builder.recommendedType;
        this.reason = builder.reason;
        this.priority = builder.priority;
        this.suggestion = builder.suggestion;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String fieldName;
        private Class<?> currentType;
        private Class<?> recommendedType;
        private String reason;
        private Priority priority;
        private String suggestion;
        
        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }
        
        public Builder currentType(Class<?> currentType) {
            this.currentType = currentType;
            return this;
        }
        
        public Builder recommendedType(Class<?> recommendedType) {
            this.recommendedType = recommendedType;
            return this;
        }
        
        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }
        
        public Builder priority(Priority priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder suggestion(String suggestion) {
            this.suggestion = suggestion;
            return this;
        }
        
        public TypeCompatibilityRecommendation build() {
            return new TypeCompatibilityRecommendation(this);
        }
    }
    
    // Priority levels
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    // Getters and Setters
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public Class<?> getCurrentType() {
        return currentType;
    }
    
    public void setCurrentType(Class<?> currentType) {
        this.currentType = currentType;
    }
    
    public Class<?> getRecommendedType() {
        return recommendedType;
    }
    
    public void setRecommendedType(Class<?> recommendedType) {
        this.recommendedType = recommendedType;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public String getSuggestion() {
        return suggestion;
    }
    
    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
    
    // Utility methods
    public String getCurrentTypeName() {
        return currentType != null ? currentType.getSimpleName() : "null";
    }
    
    public String getRecommendedTypeName() {
        return recommendedType != null ? recommendedType.getSimpleName() : "null";
    }
    
    public boolean isCritical() {
        return priority == Priority.CRITICAL || priority == Priority.HIGH;
    }
    
    @Override
    public String toString() {
        return String.format("TypeCompatibilityRecommendation{field='%s', %s -> %s, priority=%s, reason='%s'}", 
                fieldName, getCurrentTypeName(), getRecommendedTypeName(), priority, reason);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TypeCompatibilityRecommendation that = (TypeCompatibilityRecommendation) o;
        
        return fieldName.equals(that.fieldName);
    }
    
    @Override
    public int hashCode() {
        return fieldName.hashCode();
    }
}