package com.xiuxian.game.validation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry of standardized type mappings
 * Defines the canonical types to use for different database column types
 * Implements Requirements 5.1, 5.2, 5.3
 */
@Component
public class DataTypeRegistry {
    
    // Standardized type mappings for database columns
    private static final Map<String, Class<?>> STANDARDIZED_DB_TYPES = new HashMap<>();
    
    // Standardized type mappings for DTOs
    private static final Map<Class<?>, Class<?>> STANDARDIZED_DTO_TYPES = new HashMap<>();
    
    static {
        // Integer types - standardized mapping
        STANDARDIZED_DB_TYPES.put("int", Integer.class);
        STANDARDIZED_DB_TYPES.put("integer", Integer.class);
        STANDARDIZED_DB_TYPES.put("smallint", Integer.class);
        STANDARDIZED_DB_TYPES.put("mediumint", Integer.class);
        STANDARDIZED_DB_TYPES.put("tinyint", Integer.class);
        STANDARDIZED_DB_TYPES.put("bigint", Long.class);
        
        // Decimal types - standardized mapping
        STANDARDIZED_DB_TYPES.put("decimal", BigDecimal.class);
        STANDARDIZED_DB_TYPES.put("numeric", BigDecimal.class);
        STANDARDIZED_DB_TYPES.put("float", Float.class);
        STANDARDIZED_DB_TYPES.put("double", Double.class);
        STANDARDIZED_DB_TYPES.put("real", Float.class);
        
        // String types
        STANDARDIZED_DB_TYPES.put("varchar", String.class);
        STANDARDIZED_DB_TYPES.put("char", String.class);
        STANDARDIZED_DB_TYPES.put("text", String.class);
        STANDARDIZED_DB_TYPES.put("longtext", String.class);
        STANDARDIZED_DB_TYPES.put("mediumtext", String.class);
        STANDARDIZED_DB_TYPES.put("tinytext", String.class);
        STANDARDIZED_DB_TYPES.put("json", String.class);
        
        // Boolean types
        STANDARDIZED_DB_TYPES.put("tinyint(1)", Boolean.class);
        STANDARDIZED_DB_TYPES.put("boolean", Boolean.class);
        
        // Date/Time types
        STANDARDIZED_DB_TYPES.put("timestamp", LocalDateTime.class);
        STANDARDIZED_DB_TYPES.put("datetime", LocalDateTime.class);
        STANDARDIZED_DB_TYPES.put("date", LocalDateTime.class);
        
        // DTO type standardization (primitive -> wrapper)
        STANDARDIZED_DTO_TYPES.put(int.class, Integer.class);
        STANDARDIZED_DTO_TYPES.put(long.class, Long.class);
        STANDARDIZED_DTO_TYPES.put(boolean.class, Boolean.class);
        STANDARDIZED_DTO_TYPES.put(float.class, Float.class);
        STANDARDIZED_DTO_TYPES.put(double.class, Double.class);
        STANDARDIZED_DTO_TYPES.put(byte.class, Byte.class);
        STANDARDIZED_DTO_TYPES.put(short.class, Short.class);
        STANDARDIZED_DTO_TYPES.put(char.class, Character.class);
    }
    
    /**
     * Get the standardized Java type for a database column
     */
    public Class<?> getStandardizedType(ColumnInfo columnInfo) {
        String dbType = normalizeDbType(columnInfo.getDataType());
        
        // Special handling for tinyint(1) which should be Boolean
        if ("tinyint".equals(dbType) && columnInfo.getColumnSize() == 1) {
            return Boolean.class;
        }
        
        return STANDARDIZED_DB_TYPES.get(dbType);
    }
    
    /**
     * Get the recommended type for a database column
     */
    public Class<?> getRecommendedType(ColumnInfo columnInfo) {
        return getStandardizedType(columnInfo);
    }
    
    /**
     * Check if a Java type is standardized for the given database column
     */
    public boolean isStandardizedType(Class<?> javaType, ColumnInfo columnInfo) {
        Class<?> standardizedType = getStandardizedType(columnInfo);
        
        if (standardizedType == null) {
            return true; // Unknown types are considered valid
        }
        
        // Direct match
        if (javaType.equals(standardizedType)) {
            return true;
        }
        
        // Handle primitive vs wrapper compatibility
        return isPrimitiveWrapperCompatible(javaType, standardizedType);
    }
    
    /**
     * Get standardized numeric type for a column
     */
    public Class<?> getStandardizedNumericType(ColumnInfo columnInfo) {
        if (!columnInfo.isNumericType()) {
            return null;
        }
        
        String dbType = normalizeDbType(columnInfo.getDataType());
        
        // Apply standardization rules for numeric types
        switch (dbType) {
            case "int":
            case "integer":
            case "smallint":
            case "mediumint":
                return Integer.class; // Use Integer for int-based columns
                
            case "tinyint":
                return columnInfo.getColumnSize() == 1 ? Boolean.class : Integer.class;
                
            case "bigint":
                return Long.class; // Use Long for bigint columns
                
            case "decimal":
            case "numeric":
                return BigDecimal.class; // Use BigDecimal for decimal columns
                
            case "float":
            case "real":
                return Float.class;
                
            case "double":
                return Double.class;
                
            default:
                return null;
        }
    }
    
    /**
     * Check if a DTO field type is standardized
     */
    public boolean isStandardizedDTOType(Class<?> fieldType) {
        // Primitive types should be avoided in DTOs
        if (fieldType.isPrimitive()) {
            return false;
        }
        
        // Wrapper types are preferred
        return !STANDARDIZED_DTO_TYPES.containsKey(fieldType);
    }
    
    /**
     * Get recommended DTO type for a field type
     */
    public Class<?> getRecommendedDTOType(Class<?> fieldType) {
        return STANDARDIZED_DTO_TYPES.getOrDefault(fieldType, fieldType);
    }
    
    /**
     * Get type standardization rules summary
     */
    public Map<String, String> getStandardizationRules() {
        Map<String, String> rules = new HashMap<>();
        
        rules.put("int/integer/smallint/mediumint", "Use Integer in Java");
        rules.put("bigint", "Use Long in Java");
        rules.put("decimal/numeric", "Use BigDecimal in Java");
        rules.put("float/real", "Use Float in Java");
        rules.put("double", "Use Double in Java");
        rules.put("varchar/char/text", "Use String in Java");
        rules.put("tinyint(1)/boolean", "Use Boolean in Java");
        rules.put("timestamp/datetime/date", "Use LocalDateTime in Java");
        rules.put("json", "Use String in Java (or specific POJO)");
        rules.put("DTOs", "Use wrapper types (Integer, Long, Boolean) instead of primitives");
        
        return rules;
    }
    
    /**
     * Check if there are known problematic type combinations
     */
    public boolean isProblematicTypeCombination(Class<?> javaType, ColumnInfo columnInfo) {
        String dbType = normalizeDbType(columnInfo.getDataType());
        
        // Known problematic combinations
        if ("int".equals(dbType) && javaType == Long.class) {
            return true; // Long for int column can cause issues
        }
        
        if ("bigint".equals(dbType) && javaType == Integer.class) {
            return true; // Integer for bigint column can cause overflow
        }
        
        if (dbType.contains("decimal") && (javaType == Float.class || javaType == Double.class)) {
            return true; // Float/Double for decimal can lose precision
        }
        
        return false;
    }
    
    /**
     * Get severity level for type mismatch
     */
    public TypeMismatch.Severity getTypeMismatchSeverity(Class<?> javaType, ColumnInfo columnInfo) {
        if (isProblematicTypeCombination(javaType, columnInfo)) {
            return TypeMismatch.Severity.CRITICAL;
        }
        
        if (!isStandardizedType(javaType, columnInfo)) {
            return TypeMismatch.Severity.WARNING;
        }
        
        return TypeMismatch.Severity.INFO;
    }
    
    // Private helper methods
    
    private String normalizeDbType(String dbType) {
        String normalized = dbType.toLowerCase();
        
        // Remove size information for base type lookup
        if (normalized.contains("(")) {
            normalized = normalized.substring(0, normalized.indexOf("("));
        }
        
        return normalized;
    }
    
    private boolean isPrimitiveWrapperCompatible(Class<?> type1, Class<?> type2) {
        Map<Class<?>, Class<?>> primitiveToWrapper = new HashMap<>();
        primitiveToWrapper.put(int.class, Integer.class);
        primitiveToWrapper.put(long.class, Long.class);
        primitiveToWrapper.put(boolean.class, Boolean.class);
        primitiveToWrapper.put(float.class, Float.class);
        primitiveToWrapper.put(double.class, Double.class);
        primitiveToWrapper.put(byte.class, Byte.class);
        primitiveToWrapper.put(short.class, Short.class);
        primitiveToWrapper.put(char.class, Character.class);
        
        return (primitiveToWrapper.get(type1) != null && primitiveToWrapper.get(type1).equals(type2)) ||
               (primitiveToWrapper.get(type2) != null && primitiveToWrapper.get(type2).equals(type1));
    }
}