package com.xiuxian.game.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps database column types to Java types
 * Provides standardized type mapping for validation
 * Implements Requirements 4.1, 4.2, 2.2
 */
@Component
public class ColumnTypeMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(ColumnTypeMapper.class);
    
    // Standard database type to Java type mapping
    private static final Map<String, Class<?>> TYPE_MAPPING = new HashMap<>();
    
    static {
        // Integer types
        TYPE_MAPPING.put("int", Integer.class);
        TYPE_MAPPING.put("integer", Integer.class);
        TYPE_MAPPING.put("bigint", Long.class);
        TYPE_MAPPING.put("smallint", Integer.class);
        TYPE_MAPPING.put("tinyint", Integer.class);
        TYPE_MAPPING.put("mediumint", Integer.class);
        
        // Decimal types
        TYPE_MAPPING.put("decimal", BigDecimal.class);
        TYPE_MAPPING.put("numeric", BigDecimal.class);
        TYPE_MAPPING.put("float", Float.class);
        TYPE_MAPPING.put("double", Double.class);
        TYPE_MAPPING.put("real", Float.class);
        
        // String types
        TYPE_MAPPING.put("varchar", String.class);
        TYPE_MAPPING.put("char", String.class);
        TYPE_MAPPING.put("text", String.class);
        TYPE_MAPPING.put("longtext", String.class);
        TYPE_MAPPING.put("mediumtext", String.class);
        TYPE_MAPPING.put("tinytext", String.class);
        TYPE_MAPPING.put("json", String.class);
        
        // Boolean types
        TYPE_MAPPING.put("bit", Boolean.class);
        TYPE_MAPPING.put("boolean", Boolean.class);
        
        // Date/Time types
        TYPE_MAPPING.put("timestamp", LocalDateTime.class);
        TYPE_MAPPING.put("datetime", LocalDateTime.class);
        TYPE_MAPPING.put("date", LocalDateTime.class);
        TYPE_MAPPING.put("time", LocalDateTime.class);
        
        // Binary types
        TYPE_MAPPING.put("blob", byte[].class);
        TYPE_MAPPING.put("longblob", byte[].class);
        TYPE_MAPPING.put("mediumblob", byte[].class);
        TYPE_MAPPING.put("tinyblob", byte[].class);
        TYPE_MAPPING.put("binary", byte[].class);
        TYPE_MAPPING.put("varbinary", byte[].class);
    }
    
    /**
     * Map database column type to Java type
     */
    public Class<?> mapToJavaType(ColumnInfo columnInfo) {
        if (columnInfo == null) {
            return null;
        }
        
        String dbType = columnInfo.getDataType().toLowerCase();
        
        // Special handling for tinyint(1) which should be Boolean
        if (dbType.equals("tinyint") && columnInfo.getColumnSize() == 1) {
            return Boolean.class;
        }
        
        // Special handling for bit type which should be Boolean
        if (dbType.equals("bit")) {
            return Boolean.class;
        }
        
        // Handle types with parameters like varchar(255), decimal(10,2)
        String baseType = dbType;
        if (baseType.contains("(")) {
            baseType = baseType.substring(0, baseType.indexOf("("));
        }
        
        Class<?> javaType = TYPE_MAPPING.get(baseType);
        
        if (javaType == null) {
            logger.warn("Unknown database type: {} for column {}.{}", 
                    dbType, columnInfo.getTableName(), columnInfo.getColumnName());
            return Object.class; // Fallback to Object
        }
        
        return javaType;
    }
    
    /**
     * Map database column type string to Java type
     */
    public Class<?> mapToJavaType(String dbType) {
        if (dbType == null) {
            return null;
        }
        
        String normalizedType = dbType.toLowerCase();
        
        // Special handling for tinyint(1) which should be Boolean
        if (normalizedType.equals("tinyint(1)")) {
            return Boolean.class;
        }
        
        // Special handling for bit type which should be Boolean
        if (normalizedType.equals("bit") || normalizedType.startsWith("bit(")) {
            return Boolean.class;
        }
        
        // Handle types with parameters
        String baseType = normalizedType;
        if (baseType.contains("(")) {
            baseType = baseType.substring(0, baseType.indexOf("("));
        }
        
        return TYPE_MAPPING.get(baseType);
    }
    
    /**
     * Check if two types are compatible
     */
    public boolean areTypesCompatible(Class<?> javaType, ColumnInfo columnInfo) {
        Class<?> expectedType = mapToJavaType(columnInfo);
        
        if (expectedType == null || javaType == null) {
            return false;
        }
        
        // Direct match
        if (javaType.equals(expectedType)) {
            return true;
        }
        
        // Handle primitive vs wrapper types
        if (isCompatiblePrimitiveWrapper(javaType, expectedType)) {
            return true;
        }
        
        // Handle inheritance (e.g., Integer extends Number)
        if (expectedType.isAssignableFrom(javaType) || javaType.isAssignableFrom(expectedType)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if primitive and wrapper types are compatible
     */
    private boolean isCompatiblePrimitiveWrapper(Class<?> type1, Class<?> type2) {
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
    
    /**
     * Get the recommended Java type for a database column
     */
    public String getRecommendedJavaType(ColumnInfo columnInfo) {
        Class<?> javaType = mapToJavaType(columnInfo);
        
        if (javaType == null) {
            return "Object";
        }
        
        return javaType.getSimpleName();
    }
    
    /**
     * Generate type mismatch suggestion
     */
    public String generateTypeSuggestion(ColumnInfo columnInfo, Class<?> actualJavaType) {
        Class<?> expectedType = mapToJavaType(columnInfo);
        
        if (expectedType == null) {
            return "Unable to determine expected type for database column";
        }
        
        String expectedTypeName = expectedType.getSimpleName();
        String actualTypeName = actualJavaType != null ? actualJavaType.getSimpleName() : "null";
        
        return String.format("Change Java field type from %s to %s to match database column %s(%s)", 
                actualTypeName, expectedTypeName, columnInfo.getDataType(), columnInfo.getColumnSize());
    }
    
    /**
     * Check if a database type is numeric
     */
    public boolean isNumericType(String dbType) {
        String normalizedType = dbType.toLowerCase();
        if (normalizedType.contains("(")) {
            normalizedType = normalizedType.substring(0, normalizedType.indexOf("("));
        }
        
        return normalizedType.contains("int") || normalizedType.contains("decimal") || 
               normalizedType.contains("numeric") || normalizedType.contains("float") || 
               normalizedType.contains("double") || normalizedType.contains("real");
    }
    
    /**
     * Check if a database type is string type
     */
    public boolean isStringType(String dbType) {
        String normalizedType = dbType.toLowerCase();
        if (normalizedType.contains("(")) {
            normalizedType = normalizedType.substring(0, normalizedType.indexOf("("));
        }
        
        return normalizedType.contains("varchar") || normalizedType.contains("char") || 
               normalizedType.contains("text") || normalizedType.equals("json");
    }
    
    /**
     * Check if a database type is boolean type
     */
    public boolean isBooleanType(String dbType) {
        String normalizedType = dbType.toLowerCase();
        return normalizedType.equals("tinyint(1)") || 
               normalizedType.equals("boolean") || 
               normalizedType.equals("bit") ||
               normalizedType.startsWith("bit(");
    }
    
    /**
     * Check if a database type is date/time type
     */
    public boolean isDateTimeType(String dbType) {
        String normalizedType = dbType.toLowerCase();
        if (normalizedType.contains("(")) {
            normalizedType = normalizedType.substring(0, normalizedType.indexOf("("));
        }
        
        return normalizedType.contains("timestamp") || normalizedType.contains("datetime") || 
               normalizedType.contains("date") || normalizedType.contains("time");
    }
}