package com.xiuxian.game.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for safe type conversions and compatibility checking
 * Provides methods for validating type conversions across layers
 * Implements Requirements 5.1, 5.2, 5.3
 */
@Component
public class TypeConversionUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(TypeConversionUtils.class);
    
    // Type compatibility matrix
    private static final Map<Class<?>, Class<?>[]> COMPATIBLE_TYPES = new HashMap<>();
    
    static {
        // Integer type compatibility
        COMPATIBLE_TYPES.put(Integer.class, new Class<?>[]{int.class, Integer.class});
        COMPATIBLE_TYPES.put(int.class, new Class<?>[]{int.class, Integer.class});
        
        // Long type compatibility
        COMPATIBLE_TYPES.put(Long.class, new Class<?>[]{long.class, Long.class});
        COMPATIBLE_TYPES.put(long.class, new Class<?>[]{long.class, Long.class});
        
        // Boolean type compatibility
        COMPATIBLE_TYPES.put(Boolean.class, new Class<?>[]{boolean.class, Boolean.class});
        COMPATIBLE_TYPES.put(boolean.class, new Class<?>[]{boolean.class, Boolean.class});
        
        // Float type compatibility
        COMPATIBLE_TYPES.put(Float.class, new Class<?>[]{float.class, Float.class});
        COMPATIBLE_TYPES.put(float.class, new Class<?>[]{float.class, Float.class});
        
        // Double type compatibility
        COMPATIBLE_TYPES.put(Double.class, new Class<?>[]{double.class, Double.class});
        COMPATIBLE_TYPES.put(double.class, new Class<?>[]{double.class, Double.class});
        
        // String type compatibility
        COMPATIBLE_TYPES.put(String.class, new Class<?>[]{String.class});
        
        // BigDecimal type compatibility
        COMPATIBLE_TYPES.put(BigDecimal.class, new Class<?>[]{BigDecimal.class});
        
        // LocalDateTime type compatibility
        COMPATIBLE_TYPES.put(LocalDateTime.class, new Class<?>[]{LocalDateTime.class});
    }
    
    /**
     * Check if two types are compatible for conversion
     */
    public boolean areTypesCompatible(Class<?> sourceType, Class<?> targetType) {
        if (sourceType == null || targetType == null) {
            return false;
        }
        
        // Direct match
        if (sourceType.equals(targetType)) {
            return true;
        }
        
        // Check compatibility matrix
        Class<?>[] compatibleTypes = COMPATIBLE_TYPES.get(targetType);
        if (compatibleTypes != null) {
            for (Class<?> compatibleType : compatibleTypes) {
                if (compatibleType.equals(sourceType)) {
                    return true;
                }
            }
        }
        
        // Check inheritance
        if (targetType.isAssignableFrom(sourceType) || sourceType.isAssignableFrom(targetType)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if conversion from source to target type is safe
     */
    public boolean isSafeConversion(Class<?> sourceType, Class<?> targetType) {
        if (!areTypesCompatible(sourceType, targetType)) {
            return false;
        }
        
        // Check for potential data loss
        if (isNarrowingConversion(sourceType, targetType)) {
            logger.warn("Narrowing conversion detected: {} -> {}", sourceType.getSimpleName(), targetType.getSimpleName());
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if conversion is a narrowing conversion (potential data loss)
     */
    public boolean isNarrowingConversion(Class<?> sourceType, Class<?> targetType) {
        // Long to Integer is narrowing
        if (sourceType == Long.class && targetType == Integer.class) {
            return true;
        }
        
        // Double to Float is narrowing
        if (sourceType == Double.class && targetType == Float.class) {
            return true;
        }
        
        // BigDecimal to Float/Double is narrowing (precision loss)
        if (sourceType == BigDecimal.class && (targetType == Float.class || targetType == Double.class)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if conversion is a widening conversion (safe)
     */
    public boolean isWideningConversion(Class<?> sourceType, Class<?> targetType) {
        // Integer to Long is widening
        if (sourceType == Integer.class && targetType == Long.class) {
            return true;
        }
        
        // Float to Double is widening
        if (sourceType == Float.class && targetType == Double.class) {
            return true;
        }
        
        // Integer/Long to BigDecimal is widening
        if ((sourceType == Integer.class || sourceType == Long.class) && targetType == BigDecimal.class) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Get conversion risk level
     */
    public ConversionRisk getConversionRisk(Class<?> sourceType, Class<?> targetType) {
        if (!areTypesCompatible(sourceType, targetType)) {
            return ConversionRisk.INCOMPATIBLE;
        }
        
        if (sourceType.equals(targetType)) {
            return ConversionRisk.NONE;
        }
        
        if (isNarrowingConversion(sourceType, targetType)) {
            return ConversionRisk.HIGH;
        }
        
        if (isWideningConversion(sourceType, targetType)) {
            return ConversionRisk.LOW;
        }
        
        // Primitive to wrapper or vice versa
        if (isPrimitiveWrapperConversion(sourceType, targetType)) {
            return ConversionRisk.LOW;
        }
        
        return ConversionRisk.MEDIUM;
    }
    
    /**
     * Generate conversion recommendation
     */
    public String generateConversionRecommendation(Class<?> sourceType, Class<?> targetType) {
        ConversionRisk risk = getConversionRisk(sourceType, targetType);
        
        switch (risk) {
            case INCOMPATIBLE:
                return String.format("Types %s and %s are incompatible. Consider using a different approach.", 
                        sourceType.getSimpleName(), targetType.getSimpleName());
                
            case HIGH:
                return String.format("Conversion from %s to %s may cause data loss. Validate data ranges before conversion.", 
                        sourceType.getSimpleName(), targetType.getSimpleName());
                
            case MEDIUM:
                return String.format("Conversion from %s to %s should be tested thoroughly.", 
                        sourceType.getSimpleName(), targetType.getSimpleName());
                
            case LOW:
                return String.format("Conversion from %s to %s is generally safe.", 
                        sourceType.getSimpleName(), targetType.getSimpleName());
                
            case NONE:
                return "No conversion needed - types are identical.";
                
            default:
                return "Unknown conversion risk.";
        }
    }
    
    /**
     * Check if conversion is between primitive and wrapper types
     */
    public boolean isPrimitiveWrapperConversion(Class<?> sourceType, Class<?> targetType) {
        Map<Class<?>, Class<?>> primitiveToWrapper = new HashMap<>();
        primitiveToWrapper.put(int.class, Integer.class);
        primitiveToWrapper.put(long.class, Long.class);
        primitiveToWrapper.put(boolean.class, Boolean.class);
        primitiveToWrapper.put(float.class, Float.class);
        primitiveToWrapper.put(double.class, Double.class);
        primitiveToWrapper.put(byte.class, Byte.class);
        primitiveToWrapper.put(short.class, Short.class);
        primitiveToWrapper.put(char.class, Character.class);
        
        return (primitiveToWrapper.get(sourceType) != null && primitiveToWrapper.get(sourceType).equals(targetType)) ||
               (primitiveToWrapper.get(targetType) != null && primitiveToWrapper.get(targetType).equals(sourceType));
    }
    
    /**
     * Get the wrapper type for a primitive type
     */
    public Class<?> getWrapperType(Class<?> primitiveType) {
        if (!primitiveType.isPrimitive()) {
            return primitiveType;
        }
        
        Map<Class<?>, Class<?>> primitiveToWrapper = new HashMap<>();
        primitiveToWrapper.put(int.class, Integer.class);
        primitiveToWrapper.put(long.class, Long.class);
        primitiveToWrapper.put(boolean.class, Boolean.class);
        primitiveToWrapper.put(float.class, Float.class);
        primitiveToWrapper.put(double.class, Double.class);
        primitiveToWrapper.put(byte.class, Byte.class);
        primitiveToWrapper.put(short.class, Short.class);
        primitiveToWrapper.put(char.class, Character.class);
        
        return primitiveToWrapper.getOrDefault(primitiveType, primitiveType);
    }
    
    /**
     * Get the primitive type for a wrapper type
     */
    public Class<?> getPrimitiveType(Class<?> wrapperType) {
        Map<Class<?>, Class<?>> wrapperToPrimitive = new HashMap<>();
        wrapperToPrimitive.put(Integer.class, int.class);
        wrapperToPrimitive.put(Long.class, long.class);
        wrapperToPrimitive.put(Boolean.class, boolean.class);
        wrapperToPrimitive.put(Float.class, float.class);
        wrapperToPrimitive.put(Double.class, double.class);
        wrapperToPrimitive.put(Byte.class, byte.class);
        wrapperToPrimitive.put(Short.class, short.class);
        wrapperToPrimitive.put(Character.class, char.class);
        
        return wrapperToPrimitive.getOrDefault(wrapperType, wrapperType);
    }
    
    /**
     * Validate numeric type conversion for database operations
     */
    public boolean isValidDatabaseNumericConversion(Class<?> javaType, String dbType) {
        String normalizedDbType = dbType.toLowerCase();
        
        // Remove size information
        if (normalizedDbType.contains("(")) {
            normalizedDbType = normalizedDbType.substring(0, normalizedDbType.indexOf("("));
        }
        
        switch (normalizedDbType) {
            case "int":
            case "integer":
            case "smallint":
            case "mediumint":
                return javaType == Integer.class || javaType == int.class;
                
            case "bigint":
                return javaType == Long.class || javaType == long.class;
                
            case "decimal":
            case "numeric":
                return javaType == BigDecimal.class;
                
            case "float":
            case "real":
                return javaType == Float.class || javaType == float.class;
                
            case "double":
                return javaType == Double.class || javaType == double.class;
                
            case "tinyint":
                // tinyint(1) is boolean, others are integer
                return javaType == Boolean.class || javaType == boolean.class || 
                       javaType == Integer.class || javaType == int.class;
                
            default:
                return false;
        }
    }
    
    /**
     * Conversion risk levels
     */
    public enum ConversionRisk {
        NONE,           // No conversion needed
        LOW,            // Safe conversion
        MEDIUM,         // Conversion with minor risks
        HIGH,           // Conversion with data loss risk
        INCOMPATIBLE    // Types cannot be converted
    }
}