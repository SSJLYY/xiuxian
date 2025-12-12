package com.xiuxian.game.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Service for analyzing field type consistency across layers
 * Provides detailed analysis of type compatibility and recommendations
 * Implements Requirements 2.1, 2.3, 1.1
 */
@Service
public class FieldTypeAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(FieldTypeAnalyzer.class);
    
    @Autowired
    private ColumnTypeMapper columnTypeMapper;
    
    @Autowired
    private DataTypeRegistry dataTypeRegistry;
    
    @Autowired
    private TypeConversionUtils typeConversionUtils;
    
    /**
     * Analyze type consistency between entity fields and database columns
     */
    public FieldTypeAnalysisResult analyzeEntityDatabaseTypes(Class<?> entityClass, List<ColumnInfo> columns) {
        logger.debug("Analyzing type consistency for entity: {}", entityClass.getSimpleName());
        
        FieldTypeAnalysisResult result = new FieldTypeAnalysisResult();
        result.setEntityName(entityClass.getSimpleName());
        result.setAnalysisType("entity-database");
        
        try {
            Field[] entityFields = entityClass.getDeclaredFields();
            Map<String, ColumnInfo> columnMap = createColumnMap(columns);
            
            for (Field field : entityFields) {
                FieldTypeAnalysis analysis = analyzeFieldType(field, columnMap);
                if (analysis != null) {
                    result.addFieldAnalysis(analysis);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error analyzing entity-database types for {}: {}", entityClass.getSimpleName(), e.getMessage());
            result.addError("Analysis failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Analyze type consistency between DTO and entity fields
     */
    public FieldTypeAnalysisResult analyzeDTOEntityTypes(Class<?> dtoClass, Class<?> entityClass) {
        logger.debug("Analyzing type consistency: {} -> {}", dtoClass.getSimpleName(), entityClass.getSimpleName());
        
        FieldTypeAnalysisResult result = new FieldTypeAnalysisResult();
        result.setEntityName(dtoClass.getSimpleName() + " -> " + entityClass.getSimpleName());
        result.setAnalysisType("dto-entity");
        
        try {
            Field[] dtoFields = dtoClass.getDeclaredFields();
            Field[] entityFields = entityClass.getDeclaredFields();
            
            Map<String, Field> entityFieldMap = createFieldMap(entityFields);
            
            for (Field dtoField : dtoFields) {
                Field entityField = entityFieldMap.get(dtoField.getName());
                if (entityField != null) {
                    FieldTypeAnalysis analysis = analyzeDTOEntityFieldTypes(dtoField, entityField);
                    if (analysis != null) {
                        result.addFieldAnalysis(analysis);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error analyzing DTO-entity types for {} -> {}: {}", 
                    dtoClass.getSimpleName(), entityClass.getSimpleName(), e.getMessage());
            result.addError("Analysis failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Generate comprehensive type analysis report
     */
    public TypeAnalysisReport generateTypeAnalysisReport(List<Class<?>> entityClasses, List<ColumnInfo> allColumns) {
        TypeAnalysisReport report = new TypeAnalysisReport();
        
        for (Class<?> entityClass : entityClasses) {
            // Filter columns for this entity's table
            String tableName = getTableName(entityClass);
            if (tableName != null) {
                List<ColumnInfo> entityColumns = allColumns.stream()
                        .filter(col -> col.getTableName().equals(tableName))
                        .collect(java.util.stream.Collectors.toList());
                
                FieldTypeAnalysisResult result = analyzeEntityDatabaseTypes(entityClass, entityColumns);
                report.addAnalysisResult(result);
            }
        }
        
        return report;
    }
    
    /**
     * Get type compatibility recommendations
     */
    public List<TypeCompatibilityRecommendation> getTypeCompatibilityRecommendations(FieldTypeAnalysisResult analysisResult) {
        List<TypeCompatibilityRecommendation> recommendations = new ArrayList<>();
        
        for (FieldTypeAnalysis fieldAnalysis : analysisResult.getFieldAnalyses()) {
            if (!fieldAnalysis.isCompatible()) {
                TypeCompatibilityRecommendation recommendation = TypeCompatibilityRecommendation.builder()
                        .fieldName(fieldAnalysis.getFieldName())
                        .currentType(fieldAnalysis.getActualType())
                        .recommendedType(fieldAnalysis.getExpectedType())
                        .reason(fieldAnalysis.getIncompatibilityReason())
                        .priority(determinePriority(fieldAnalysis))
                        .suggestion(fieldAnalysis.getSuggestion())
                        .build();
                
                recommendations.add(recommendation);
            }
        }
        
        return recommendations;
    }
    
    // Private helper methods
    
    private FieldTypeAnalysis analyzeFieldType(Field field, Map<String, ColumnInfo> columnMap) {
        String columnName = getColumnName(field);
        ColumnInfo columnInfo = columnMap.get(columnName);
        
        if (columnInfo == null) {
            return null; // Field doesn't map to a database column
        }
        
        FieldTypeAnalysis analysis = new FieldTypeAnalysis();
        analysis.setFieldName(field.getName());
        analysis.setActualType(field.getType());
        
        Class<?> expectedType = columnTypeMapper.mapToJavaType(columnInfo);
        analysis.setExpectedType(expectedType);
        
        boolean compatible = columnTypeMapper.areTypesCompatible(field.getType(), columnInfo);
        analysis.setCompatible(compatible);
        
        if (!compatible) {
            analysis.setIncompatibilityReason(generateIncompatibilityReason(field.getType(), expectedType, columnInfo));
            analysis.setSuggestion(columnTypeMapper.generateTypeSuggestion(columnInfo, field.getType()));
        }
        
        // Additional analysis
        analysis.setStandardized(dataTypeRegistry.isStandardizedType(field.getType(), columnInfo));
        analysis.setConversionRisk(typeConversionUtils.getConversionRisk(field.getType(), expectedType));
        
        return analysis;
    }
    
    private FieldTypeAnalysis analyzeDTOEntityFieldTypes(Field dtoField, Field entityField) {
        FieldTypeAnalysis analysis = new FieldTypeAnalysis();
        analysis.setFieldName(dtoField.getName());
        analysis.setActualType(dtoField.getType());
        analysis.setExpectedType(entityField.getType());
        
        boolean compatible = typeConversionUtils.areTypesCompatible(dtoField.getType(), entityField.getType());
        analysis.setCompatible(compatible);
        
        if (!compatible) {
            analysis.setIncompatibilityReason(String.format("DTO field type %s is not compatible with entity field type %s", 
                    dtoField.getType().getSimpleName(), entityField.getType().getSimpleName()));
            analysis.setSuggestion(String.format("Change DTO field type to %s to match entity", 
                    entityField.getType().getSimpleName()));
        }
        
        analysis.setConversionRisk(typeConversionUtils.getConversionRisk(dtoField.getType(), entityField.getType()));
        
        return analysis;
    }
    
    private String generateIncompatibilityReason(Class<?> actualType, Class<?> expectedType, ColumnInfo columnInfo) {
        if (expectedType == null) {
            return "Unable to determine expected type for database column " + columnInfo.getDataType();
        }
        
        if (dataTypeRegistry.isProblematicTypeCombination(actualType, columnInfo)) {
            return String.format("Problematic type combination: %s for %s column can cause data loss or overflow", 
                    actualType.getSimpleName(), columnInfo.getDataType());
        }
        
        return String.format("Type mismatch: expected %s for %s column, but found %s", 
                expectedType.getSimpleName(), columnInfo.getDataType(), actualType.getSimpleName());
    }
    
    private TypeCompatibilityRecommendation.Priority determinePriority(FieldTypeAnalysis fieldAnalysis) {
        if (fieldAnalysis.getConversionRisk() == TypeConversionUtils.ConversionRisk.HIGH) {
            return TypeCompatibilityRecommendation.Priority.HIGH;
        } else if (fieldAnalysis.getConversionRisk() == TypeConversionUtils.ConversionRisk.MEDIUM) {
            return TypeCompatibilityRecommendation.Priority.MEDIUM;
        } else {
            return TypeCompatibilityRecommendation.Priority.LOW;
        }
    }
    
    private Map<String, ColumnInfo> createColumnMap(List<ColumnInfo> columns) {
        Map<String, ColumnInfo> columnMap = new HashMap<>();
        for (ColumnInfo column : columns) {
            columnMap.put(column.getColumnName(), column);
        }
        return columnMap;
    }
    
    private Map<String, Field> createFieldMap(Field[] fields) {
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : fields) {
            fieldMap.put(field.getName(), field);
        }
        return fieldMap;
    }
    
    private String getColumnName(Field field) {
        // This should match the logic in other validators
        return field.getName(); // Simplified for now
    }
    
    private String getTableName(Class<?> entityClass) {
        // This should match the logic in other validators
        return null; // Simplified for now - would need @TableName annotation logic
    }
}