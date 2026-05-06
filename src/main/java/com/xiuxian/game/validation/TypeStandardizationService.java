package com.xiuxian.game.validation;

import com.baomidou.mybatisplus.annotation.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Service for type standardization across layers
 * Implements cross-layer validation and type conflict resolution
 * Implements Requirements 1.4, 5.1, 5.2, 5.3
 */
@Service
public class TypeStandardizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TypeStandardizationService.class);
    
    @Autowired
    private SchemaAnalyzer schemaAnalyzer;
    
    @Autowired
    private ColumnTypeMapper columnTypeMapper;
    
    @Autowired
    private DataTypeRegistry dataTypeRegistry;
    
    @Autowired
    private TypeConversionUtils typeConversionUtils;
    
    /**
     * Validate entity consistency with standardized types
     * Property 3: Numeric Type Standardization
     */
    public TypeValidationResult validateEntityConsistency(Class<?> entityClass) {
        logger.info("Validating type standardization for entity: {}", entityClass.getSimpleName());
        
        TypeValidationResult result = TypeValidationResult.builder()
                .entityName(entityClass.getSimpleName())
                .build();
        
        try {
            String tableName = getTableName(entityClass);
            if (tableName == null) {
                result.addWarning("No @TableName annotation found for entity: " + entityClass.getSimpleName());
                return result;
            }
            
            result.setTableName(tableName);
            
            // Get database schema
            List<ColumnInfo> columns = schemaAnalyzer.getTableColumns(tableName);
            if (columns.isEmpty()) {
                result.addWarning("No database columns found for table: " + tableName);
                return result;
            }
            
            // Validate each field against standardized types
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                validateFieldTypeStandardization(field, columns, result);
            }
            
        } catch (Exception e) {
            logger.error("Error validating type standardization for {}: {}", entityClass.getSimpleName(), e.getMessage());
            result.addMismatch(createValidationErrorMismatch(entityClass.getSimpleName(), e.getMessage()));
        }
        
        return result;
    }
    
    /**
     * Validate DTO consistency with standardized types
     */
    public TypeValidationResult validateDTOConsistency(Class<?> dtoClass) {
        logger.info("Validating DTO type standardization for: {}", dtoClass.getSimpleName());
        
        TypeValidationResult result = TypeValidationResult.builder()
                .entityName(dtoClass.getSimpleName())
                .build();
        
        try {
            Field[] fields = dtoClass.getDeclaredFields();
            for (Field field : fields) {
                validateDTOFieldTypeStandardization(field, result);
            }
            
        } catch (Exception e) {
            logger.error("Error validating DTO type standardization for {}: {}", dtoClass.getSimpleName(), e.getMessage());
            result.addMismatch(createValidationErrorMismatch(dtoClass.getSimpleName(), e.getMessage()));
        }
        
        return result;
    }
    
    /**
     * Find type mismatches between entity and database
     * Property 4: Type Conflict Resolution
     */
    public Map<String, TypeMismatch> findTypeMismatches(String tableName, Class<?> entityClass) {
        Map<String, TypeMismatch> mismatches = new HashMap<>();
        
        try {
            List<ColumnInfo> columns = schemaAnalyzer.getTableColumns(tableName);
            Field[] fields = entityClass.getDeclaredFields();
            
            for (Field field : fields) {
                String columnName = getColumnName(field);
                Optional<ColumnInfo> columnInfoOpt = findColumnInfo(columns, columnName);
                
                if (columnInfoOpt.isPresent()) {
                    ColumnInfo columnInfo = columnInfoOpt.get();
                    Class<?> fieldType = field.getType();
                    Class<?> expectedType = columnTypeMapper.mapToJavaType(columnInfo);
                    
                    if (!typeConversionUtils.areTypesCompatible(fieldType, expectedType)) {
                        TypeMismatch mismatch = createTypeMismatch(field, columnInfo, fieldType, expectedType, entityClass.getSimpleName());
                        mismatches.put(field.getName(), mismatch);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error finding type mismatches for {}: {}", entityClass.getSimpleName(), e.getMessage());
        }
        
        return mismatches;
    }
    
    /**
     * Standardize numeric types for a table
     * Property 3: Numeric Type Standardization
     */
    public List<TypeStandardizationRecommendation> standardizeNumericTypes(String tableName) {
        List<TypeStandardizationRecommendation> recommendations = new ArrayList<>();
        
        try {
            List<ColumnInfo> columns = schemaAnalyzer.getTableColumns(tableName);
            
            for (ColumnInfo column : columns) {
                if (column.isNumericType()) {
                    TypeStandardizationRecommendation recommendation = analyzeNumericTypeStandardization(column);
                    if (recommendation != null) {
                        recommendations.add(recommendation);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error standardizing numeric types for table {}: {}", tableName, e.getMessage());
        }
        
        return recommendations;
    }
    
    /**
     * Get comprehensive type standardization report
     */
    public TypeStandardizationReport generateStandardizationReport(List<Class<?>> entityClasses) {
        TypeStandardizationReport report = new TypeStandardizationReport();
        
        for (Class<?> entityClass : entityClasses) {
            TypeValidationResult result = validateEntityConsistency(entityClass);
            report.addEntityResult(result);
            
            String tableName = getTableName(entityClass);
            if (tableName != null) {
                Map<String, TypeMismatch> mismatches = findTypeMismatches(tableName, entityClass);
                report.addTypeMismatches(entityClass.getSimpleName(), mismatches);
                
                List<TypeStandardizationRecommendation> recommendations = standardizeNumericTypes(tableName);
                report.addRecommendations(tableName, recommendations);
            }
        }
        
        return report;
    }
    
    // Private helper methods
    
    private void validateFieldTypeStandardization(Field field, List<ColumnInfo> columns, TypeValidationResult result) {
        String columnName = getColumnName(field);
        Optional<ColumnInfo> columnInfoOpt = findColumnInfo(columns, columnName);
        
        if (!columnInfoOpt.isPresent()) {
            return; // Skip fields without corresponding database columns
        }
        
        ColumnInfo columnInfo = columnInfoOpt.get();
        Class<?> fieldType = field.getType();
        
        // Check if field type follows standardization rules
        if (!dataTypeRegistry.isStandardizedType(fieldType, columnInfo)) {
            Class<?> recommendedType = dataTypeRegistry.getRecommendedType(columnInfo);
            
            TypeMismatch mismatch = TypeMismatch.builder()
                    .fieldName(field.getName())
                    .expectedType(recommendedType != null ? recommendedType.getSimpleName() : "Unknown")
                    .actualType(fieldType.getSimpleName())
                    .layer("type-standardization")
                    .entityName(result.getEntityName())
                    .tableName(result.getTableName())
                    .severity(TypeMismatch.Severity.WARNING)
                    .suggestion(generateStandardizationSuggestion(field, columnInfo, recommendedType))
                    .build();
            
            result.addMismatch(mismatch);
        }
    }
    
    private void validateDTOFieldTypeStandardization(Field field, TypeValidationResult result) {
        Class<?> fieldType = field.getType();
        
        // Check if DTO field type follows standardization conventions
        if (!dataTypeRegistry.isStandardizedDTOType(fieldType)) {
            Class<?> recommendedType = dataTypeRegistry.getRecommendedDTOType(fieldType);
            
            if (recommendedType != null && !recommendedType.equals(fieldType)) {
                TypeMismatch mismatch = TypeMismatch.builder()
                        .fieldName(field.getName())
                        .expectedType(recommendedType.getSimpleName())
                        .actualType(fieldType.getSimpleName())
                        .layer("dto-standardization")
                        .entityName(result.getEntityName())
                        .severity(TypeMismatch.Severity.WARNING)
                        .suggestion(String.format("Consider using %s instead of %s for DTO field %s", 
                                recommendedType.getSimpleName(), fieldType.getSimpleName(), field.getName()))
                        .build();
                
                result.addMismatch(mismatch);
            }
        }
    }
    
    private TypeStandardizationRecommendation analyzeNumericTypeStandardization(ColumnInfo column) {
        Class<?> currentRecommendedType = columnTypeMapper.mapToJavaType(column);
        Class<?> standardizedType = dataTypeRegistry.getStandardizedNumericType(column);
        
        if (currentRecommendedType != null && standardizedType != null && 
            !currentRecommendedType.equals(standardizedType)) {
            
            return TypeStandardizationRecommendation.builder()
                    .tableName(column.getTableName())
                    .columnName(column.getColumnName())
                    .currentType(currentRecommendedType)
                    .recommendedType(standardizedType)
                    .reason(generateNumericStandardizationReason(column, currentRecommendedType, standardizedType))
                    .priority(determineRecommendationPriority(column, currentRecommendedType, standardizedType))
                    .build();
        }
        
        return null;
    }
    
    private String generateStandardizationSuggestion(Field field, ColumnInfo columnInfo, Class<?> recommendedType) {
        if (recommendedType == null) {
            return "Review field type for standardization compliance";
        }
        
        return String.format("Change field %s from %s to %s to follow standardization rules for %s columns", 
                field.getName(), field.getType().getSimpleName(), recommendedType.getSimpleName(), 
                columnInfo.getDataType());
    }
    
    private String generateNumericStandardizationReason(ColumnInfo column, Class<?> currentType, Class<?> standardizedType) {
        String dbType = column.getDataType().toLowerCase();
        
        if (dbType.contains("int") && !dbType.equals("bigint")) {
            return "Use Integer for int database columns to ensure consistency";
        } else if (dbType.equals("bigint")) {
            return "Use Long for bigint database columns to handle large values";
        } else if (dbType.contains("decimal")) {
            return "Use BigDecimal for decimal database columns to maintain precision";
        }
        
        return "Follow numeric type standardization conventions";
    }
    
    private TypeStandardizationRecommendation.Priority determineRecommendationPriority(
            ColumnInfo column, Class<?> currentType, Class<?> standardizedType) {
        
        // High priority for int/long conflicts
        if ((currentType == Long.class && standardizedType == Integer.class) ||
            (currentType == Integer.class && standardizedType == Long.class)) {
            return TypeStandardizationRecommendation.Priority.HIGH;
        }
        
        // Medium priority for decimal precision issues
        if (column.getDataType().toLowerCase().contains("decimal")) {
            return TypeStandardizationRecommendation.Priority.MEDIUM;
        }
        
        return TypeStandardizationRecommendation.Priority.LOW;
    }
    
    private TypeMismatch createTypeMismatch(Field field, ColumnInfo columnInfo, Class<?> fieldType, 
                                          Class<?> expectedType, String entityName) {
        return TypeMismatch.builder()
                .fieldName(field.getName())
                .expectedType(expectedType != null ? expectedType.getSimpleName() : "Unknown")
                .actualType(fieldType.getSimpleName())
                .layer("type-standardization")
                .entityName(entityName)
                .tableName(columnInfo.getTableName())
                .severity(TypeMismatch.Severity.WARNING)
                .suggestion(columnTypeMapper.generateTypeSuggestion(columnInfo, fieldType))
                .build();
    }
    
    private TypeMismatch createValidationErrorMismatch(String entityName, String errorMessage) {
        return TypeMismatch.builder()
                .fieldName("validation")
                .expectedType("success")
                .actualType("error")
                .layer("type-standardization")
                .entityName(entityName)
                .severity(TypeMismatch.Severity.CRITICAL)
                .suggestion("Check entity configuration and database connectivity: " + errorMessage)
                .build();
    }
    
    private String getTableName(Class<?> entityClass) {
        TableName tableNameAnnotation = entityClass.getAnnotation(TableName.class);
        return tableNameAnnotation != null ? tableNameAnnotation.value() : null;
    }
    
    private String getColumnName(Field field) {
        // This should match the logic in DataConsistencyValidator
        return field.getName();
    }
    
    private Optional<ColumnInfo> findColumnInfo(List<ColumnInfo> columns, String columnName) {
        return columns.stream()
                .filter(col -> col.getColumnName().equalsIgnoreCase(columnName))
                .findFirst();
    }
}
