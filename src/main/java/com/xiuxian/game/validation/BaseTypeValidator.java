package com.xiuxian.game.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for type validation utilities
 * Provides common functionality for type checking and mapping
 */
@Component
public class BaseTypeValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(BaseTypeValidator.class);
    
    // Database type to Java type mapping
    private static final Map<String, Class<?>> DB_TO_JAVA_TYPE_MAP = new HashMap<>();
    
    static {
        // Integer types
        DB_TO_JAVA_TYPE_MAP.put("int", Integer.class);
        DB_TO_JAVA_TYPE_MAP.put("integer", Integer.class);
        DB_TO_JAVA_TYPE_MAP.put("bigint", Long.class);
        DB_TO_JAVA_TYPE_MAP.put("smallint", Integer.class);
        DB_TO_JAVA_TYPE_MAP.put("tinyint", Integer.class);
        
        // Decimal types
        DB_TO_JAVA_TYPE_MAP.put("decimal", BigDecimal.class);
        DB_TO_JAVA_TYPE_MAP.put("numeric", BigDecimal.class);
        DB_TO_JAVA_TYPE_MAP.put("float", Float.class);
        DB_TO_JAVA_TYPE_MAP.put("double", Double.class);
        
        // String types
        DB_TO_JAVA_TYPE_MAP.put("varchar", String.class);
        DB_TO_JAVA_TYPE_MAP.put("char", String.class);
        DB_TO_JAVA_TYPE_MAP.put("text", String.class);
        DB_TO_JAVA_TYPE_MAP.put("longtext", String.class);
        
        // Boolean types
        DB_TO_JAVA_TYPE_MAP.put("tinyint(1)", Boolean.class);
        DB_TO_JAVA_TYPE_MAP.put("boolean", Boolean.class);
        
        // Date/Time types
        DB_TO_JAVA_TYPE_MAP.put("timestamp", LocalDateTime.class);
        DB_TO_JAVA_TYPE_MAP.put("datetime", LocalDateTime.class);
        DB_TO_JAVA_TYPE_MAP.put("date", LocalDateTime.class);
    }
    
    /**
     * Get the expected Java type for a database column type
     */
    public Class<?> getExpectedJavaType(String dbType) {
        if (dbType == null) {
            return null;
        }
        
        // Handle types with parameters like varchar(255), decimal(10,2)
        String baseType = dbType.toLowerCase();
        if (baseType.contains("(")) {
            baseType = baseType.substring(0, baseType.indexOf("("));
        }
        
        // Special case for tinyint(1) which should be Boolean
        if (dbType.toLowerCase().equals("tinyint(1)")) {
            return Boolean.class;
        }
        
        return DB_TO_JAVA_TYPE_MAP.get(baseType);
    }
    
    /**
     * Check if two types are compatible
     */
    public boolean areTypesCompatible(Class<?> javaType, String dbType) {
        Class<?> expectedType = getExpectedJavaType(dbType);
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
        
        return (primitiveToWrapper.get(type1) != null && primitiveToWrapper.get(type1).equals(type2)) ||
               (primitiveToWrapper.get(type2) != null && primitiveToWrapper.get(type2).equals(type1));
    }
    
    /**
     * Get all fields from a class including inherited fields
     */
    public Field[] getAllFields(Class<?> clazz) {
        return clazz.getDeclaredFields();
    }
    
    /**
     * Create a type mismatch record
     */
    public TypeMismatch createTypeMismatch(String fieldName, String expectedType, String actualType, 
                                         String layer, String entityName, String tableName, 
                                         TypeMismatch.Severity severity) {
        String suggestion = generateSuggestion(fieldName, expectedType, actualType, layer);
        
        return TypeMismatch.builder()
                .fieldName(fieldName)
                .expectedType(expectedType)
                .actualType(actualType)
                .layer(layer)
                .entityName(entityName)
                .tableName(tableName)
                .suggestion(suggestion)
                .severity(severity)
                .build();
    }
    
    /**
     * Generate helpful suggestions for fixing type mismatches
     */
    private String generateSuggestion(String fieldName, String expectedType, String actualType, String layer) {
        if ("backend-database".equals(layer)) {
            if ("Long".equals(actualType) && "Integer".equals(expectedType)) {
                return "Change entity field type from Long to Integer to match database int column";
            }
            if ("Integer".equals(actualType) && "Long".equals(expectedType)) {
                return "Change entity field type from Integer to Long to match database bigint column";
            }
        }
        
        return String.format("Ensure %s field uses %s type in %s layer", fieldName, expectedType, layer);
    }
    
    /**
     * Log validation results
     */
    public void logValidationResult(TypeValidationResult result) {
        if (result.isValid()) {
            logger.info("Type validation passed for entity: {}", result.getEntityName());
        } else {
            logger.error("Type validation failed for entity: {}", result.getEntityName());
            for (TypeMismatch mismatch : result.getMismatches()) {
                logger.error("  {}", mismatch.toString());
            }
        }
        
        for (String warning : result.getWarnings()) {
            logger.warn("  {}", warning);
        }
    }
}