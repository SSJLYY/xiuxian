package com.xiuxian.game.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Service for validating API response completeness and correctness
 * Ensures API responses contain all required fields with correct data types
 * Implements Requirements 1.5
 */
@Service
public class APIResponseValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(APIResponseValidator.class);
    
    @Autowired
    private DataTypeRegistry dataTypeRegistry;
    
    @Autowired
    private TypeConversionUtils typeConversionUtils;
    
    /**
     * Validate API response completeness
     * Property 5: API Response Completeness
     */
    public APIResponseValidationResult validateResponseCompleteness(Class<?> responseClass, Object responseInstance) {
        logger.debug("Validating API response completeness for: {}", responseClass.getSimpleName());
        
        APIResponseValidationResult result = new APIResponseValidationResult();
        result.setResponseClassName(responseClass.getSimpleName());
        
        try {
            Field[] fields = responseClass.getDeclaredFields();
            
            for (Field field : fields) {
                APIFieldValidation fieldValidation = validateResponseField(field, responseInstance);
                result.addFieldValidation(fieldValidation);
            }
            
        } catch (Exception e) {
            logger.error("Error validating API response completeness for {}: {}", responseClass.getSimpleName(), e.getMessage());
            result.addError("Validation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Validate API response against expected schema
     */
    public APIResponseValidationResult validateResponseSchema(Class<?> responseClass, Map<String, Class<?>> expectedSchema) {
        logger.debug("Validating API response schema for: {}", responseClass.getSimpleName());
        
        APIResponseValidationResult result = new APIResponseValidationResult();
        result.setResponseClassName(responseClass.getSimpleName());
        
        try {
            Field[] fields = responseClass.getDeclaredFields();
            Map<String, Field> fieldMap = createFieldMap(fields);
            
            // Check for missing fields in response
            for (Map.Entry<String, Class<?>> entry : expectedSchema.entrySet()) {
                String expectedFieldName = entry.getKey();
                Class<?> expectedType = entry.getValue();
                
                Field responseField = fieldMap.get(expectedFieldName);
                if (responseField == null) {
                    APIFieldValidation fieldValidation = new APIFieldValidation();
                    fieldValidation.setFieldName(expectedFieldName);
                    fieldValidation.setExpectedType(expectedType);
                    fieldValidation.setPresent(false);
                    fieldValidation.setValid(false);
                    fieldValidation.setIssue("Field missing from API response");
                    fieldValidation.setSuggestion("Add field '" + expectedFieldName + "' to response class");
                    
                    result.addFieldValidation(fieldValidation);
                } else {
                    // Validate field type
                    APIFieldValidation fieldValidation = validateFieldType(responseField, expectedType);
                    result.addFieldValidation(fieldValidation);
                }
            }
            
            // Check for extra fields in response
            for (Field field : fields) {
                if (!expectedSchema.containsKey(field.getName())) {
                    APIFieldValidation fieldValidation = new APIFieldValidation();
                    fieldValidation.setFieldName(field.getName());
                    fieldValidation.setActualType(field.getType());
                    fieldValidation.setPresent(true);
                    fieldValidation.setValid(true); // Extra fields are not invalid, just unexpected
                    fieldValidation.setIssue("Unexpected field in API response");
                    fieldValidation.setSuggestion("Consider removing field '" + field.getName() + "' if not needed");
                    
                    result.addFieldValidation(fieldValidation);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error validating API response schema for {}: {}", responseClass.getSimpleName(), e.getMessage());
            result.addError("Schema validation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Validate multiple API response classes
     */
    public List<APIResponseValidationResult> validateMultipleResponses(List<Class<?>> responseClasses) {
        List<APIResponseValidationResult> results = new ArrayList<>();
        
        for (Class<?> responseClass : responseClasses) {
            APIResponseValidationResult result = validateResponseCompleteness(responseClass, null);
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * Generate API response validation report
     */
    public APIResponseValidationReport generateValidationReport(List<Class<?>> responseClasses) {
        APIResponseValidationReport report = new APIResponseValidationReport();
        
        for (Class<?> responseClass : responseClasses) {
            APIResponseValidationResult result = validateResponseCompleteness(responseClass, null);
            report.addValidationResult(result);
        }
        
        return report;
    }
    
    /**
     * Check if response class follows API design best practices
     */
    public List<APIDesignRecommendation> getAPIDesignRecommendations(Class<?> responseClass) {
        List<APIDesignRecommendation> recommendations = new ArrayList<>();
        
        try {
            Field[] fields = responseClass.getDeclaredFields();
            
            for (Field field : fields) {
                // Check for primitive types (should use wrapper types in APIs)
                if (field.getType().isPrimitive()) {
                    recommendations.add(APIDesignRecommendation.builder()
                            .fieldName(field.getName())
                            .currentType(field.getType())
                            .recommendedType(typeConversionUtils.getWrapperType(field.getType()))
                            .reason("Primitive types can cause issues with null values in APIs")
                            .priority(APIDesignRecommendation.Priority.MEDIUM)
                            .suggestion("Use wrapper type " + typeConversionUtils.getWrapperType(field.getType()).getSimpleName() + " instead")
                            .build());
                }
                
                // Check for standardized DTO types
                if (!dataTypeRegistry.isStandardizedDTOType(field.getType())) {
                    Class<?> recommendedType = dataTypeRegistry.getRecommendedDTOType(field.getType());
                    if (recommendedType != null && !recommendedType.equals(field.getType())) {
                        recommendations.add(APIDesignRecommendation.builder()
                                .fieldName(field.getName())
                                .currentType(field.getType())
                                .recommendedType(recommendedType)
                                .reason("Field type does not follow API standardization guidelines")
                                .priority(APIDesignRecommendation.Priority.LOW)
                                .suggestion("Consider using " + recommendedType.getSimpleName() + " for better API consistency")
                                .build());
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error generating API design recommendations for {}: {}", responseClass.getSimpleName(), e.getMessage());
        }
        
        return recommendations;
    }
    
    // Private helper methods
    
    private APIFieldValidation validateResponseField(Field field, Object responseInstance) {
        APIFieldValidation fieldValidation = new APIFieldValidation();
        fieldValidation.setFieldName(field.getName());
        fieldValidation.setActualType(field.getType());
        fieldValidation.setPresent(true);
        
        try {
            // Check if field is accessible
            field.setAccessible(true);
            
            // If we have an instance, check the actual value
            if (responseInstance != null) {
                Object value = field.get(responseInstance);
                fieldValidation.setHasValue(value != null);
                
                if (value != null) {
                    // Validate that the value type matches the field type
                    if (!field.getType().isAssignableFrom(value.getClass())) {
                        fieldValidation.setValid(false);
                        fieldValidation.setIssue("Field value type does not match declared type");
                        fieldValidation.setSuggestion("Ensure field value matches declared type " + field.getType().getSimpleName());
                    } else {
                        fieldValidation.setValid(true);
                    }
                } else {
                    // Null value - check if field type can handle nulls
                    if (field.getType().isPrimitive()) {
                        fieldValidation.setValid(false);
                        fieldValidation.setIssue("Primitive field cannot be null");
                        fieldValidation.setSuggestion("Use wrapper type " + typeConversionUtils.getWrapperType(field.getType()).getSimpleName());
                    } else {
                        fieldValidation.setValid(true);
                    }
                }
            } else {
                // No instance provided, just validate the field declaration
                fieldValidation.setValid(true);
                fieldValidation.setHasValue(false);
            }
            
        } catch (Exception e) {
            fieldValidation.setValid(false);
            fieldValidation.setIssue("Error accessing field: " + e.getMessage());
            fieldValidation.setSuggestion("Check field accessibility and type compatibility");
        }
        
        return fieldValidation;
    }
    
    private APIFieldValidation validateFieldType(Field field, Class<?> expectedType) {
        APIFieldValidation fieldValidation = new APIFieldValidation();
        fieldValidation.setFieldName(field.getName());
        fieldValidation.setActualType(field.getType());
        fieldValidation.setExpectedType(expectedType);
        fieldValidation.setPresent(true);
        
        boolean compatible = typeConversionUtils.areTypesCompatible(field.getType(), expectedType);
        fieldValidation.setValid(compatible);
        
        if (!compatible) {
            fieldValidation.setIssue("Field type does not match expected type");
            fieldValidation.setSuggestion("Change field type from " + field.getType().getSimpleName() + 
                    " to " + expectedType.getSimpleName());
        }
        
        return fieldValidation;
    }
    
    private Map<String, Field> createFieldMap(Field[] fields) {
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : fields) {
            fieldMap.put(field.getName(), field);
        }
        return fieldMap;
    }
}