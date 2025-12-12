package com.xiuxian.game.validation;

/**
 * Represents a type standardization recommendation
 * Used to suggest improvements to type consistency
 */
public class TypeStandardizationRecommendation {
    
    private String tableName;
    private String columnName;
    private Class<?> currentType;
    private Class<?> recommendedType;
    private String reason;
    private Priority priority;
    
    // Constructors
    public TypeStandardizationRecommendation() {}
    
    private TypeStandardizationRecommendation(Builder builder) {
        this.tableName = builder.tableName;
        this.columnName = builder.columnName;
        this.currentType = builder.currentType;
        this.recommendedType = builder.recommendedType;
        this.reason = builder.reason;
        this.priority = builder.priority;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String tableName;
        private String columnName;
        private Class<?> currentType;
        private Class<?> recommendedType;
        private String reason;
        private Priority priority;
        
        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }
        
        public Builder columnName(String columnName) {
            this.columnName = columnName;
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
        
        public TypeStandardizationRecommendation build() {
            return new TypeStandardizationRecommendation(this);
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
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    public void setColumnName(String columnName) {
        this.columnName = columnName;
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
    
    @Override
    public String toString() {
        return String.format("TypeStandardizationRecommendation{table='%s', column='%s', %s -> %s, priority=%s, reason='%s'}", 
                tableName, columnName, 
                currentType != null ? currentType.getSimpleName() : "null",
                recommendedType != null ? recommendedType.getSimpleName() : "null",
                priority, reason);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TypeStandardizationRecommendation that = (TypeStandardizationRecommendation) o;
        
        if (!tableName.equals(that.tableName)) return false;
        return columnName.equals(that.columnName);
    }
    
    @Override
    public int hashCode() {
        int result = tableName.hashCode();
        result = 31 * result + columnName.hashCode();
        return result;
    }
}