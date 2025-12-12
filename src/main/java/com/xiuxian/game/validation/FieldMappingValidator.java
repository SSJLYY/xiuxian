package com.xiuxian.game.validation;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for validating field mappings between layers
 * Ensures all fields are properly mapped and consistent
 */
@Service
public class FieldMappingValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(FieldMappingValidator.class);
    
    @Autowired
    private BaseTypeValidator baseTypeValidator;
    
    @Autowired
    private SchemaAnalyzer schemaAnalyzer;
    
    @Autowired
    private ColumnTypeMapper columnTypeMapper;
    
    /**
     * Validate field mappings between entity and database
     * Property 1: Frontend-Backend Field Consistency
     */
    public TypeValidationResult validateEntityToDatabase(Class<?> entityClass, String tableName) {
        logger.debug("Validating field mappings for entity: {} -> table: {}", 
                entityClass.getSimpleName(), tableName);
        
        TypeValidationResult result = TypeValidationResult.builder()
                .entityName(entityClass.getSimpleName())
                .tableName(tableName)
                .build();
        
        try {
            // Get database schema information
            List<ColumnInfo> columns = schemaAnalyzer.getTableColumns(tableName);
            if (columns.isEmpty()) {
                result.addWarning("No database columns found for table: " + tableName);
                return result;
            }
            
            // Get entity fields
            Field[] entityFields = baseTypeValidator.getAllFields(entityClass);
            Map<String, Field> entityFieldMap = getEntityFieldMap(entityFields);
            Map<String, ColumnInfo> columnMap = getColumnMap(columns);
            
            // Validate field mappings
            validateFieldMappings(entityFieldMap, columnMap, result);
            
            // Check for missing fields
            findMissingFields(entityFieldMap, columnMap, result);
            
        } catch (Exception e) {
            logger.error("Error validating field mappings for {}: {}", entityClass.getSimpleName(), e.getMessage());
            result.addMismatch(createValidationErrorMismatch(entityClass.getSimpleName(), e.getMessage()));
        }
        
        return result;
    }
    
    /**
     * Validate field mappings between DTO and entity
     */
    public TypeValidationResult validateDTOToEntity(Class<?> dtoClass, Class<?> entityClass) {
        logger.debug("Validating field mappings: {} -> {}", 
                dtoClass.getSimpleName(), entityClass.getSimpleName());
        
        TypeValidationResult result = TypeValidationResult.builder()
                .entityName(dtoClass.getSimpleName() + " -> " + entityClass.getSimpleName())
                .build();
        
        Field[] dtoFields = baseTypeValidator.getAllFields(dtoClass);
        Field[] entityFields = baseTypeValidator.getAllFields(entityClass);
        
        Set<String> dtoFieldNames = getEntityFieldNames(dtoFields);
        Set<String> entityFieldNames = getEntityFieldNames(entityFields);
        
        // Find fields that exist in DTO but not in entity
        List<String> extraInDto = dtoFieldNames.stream()
                .filter(fieldName -> !entityFieldNames.contains(fieldName))
                .collect(Collectors.toList());
        
        // Find fields that exist in entity but not in DTO
        List<String> missingInDto = entityFieldNames.stream()
                .filter(fieldName -> !dtoFieldNames.contains(fieldName))
                .collect(Collectors.toList());
        
        // Log findings
        for (String extraField : extraInDto) {
            result.addWarning(String.format("DTO field '%s' has no corresponding entity field", extraField));
        }
        
        for (String missingField : missingInDto) {
            result.addWarning(String.format("Entity field '%s' has no corresponding DTO field", missingField));
        }
        
        return result;
    }
    
    /**
     * Find missing fields between entity and database
     */
    public List<MissingField> findMissingFields(String tableName, Class<?> entityClass) {
        List<MissingField> missingFields = new ArrayList<>();
        
        try {
            List<ColumnInfo> columns = schemaAnalyzer.getTableColumns(tableName);
            Field[] entityFields = baseTypeValidator.getAllFields(entityClass);
            
            Map<String, Field> entityFieldMap = getEntityFieldMap(entityFields);
            Map<String, ColumnInfo> columnMap = getColumnMap(columns);
            
            // Fields missing in database
            for (String fieldName : entityFieldMap.keySet()) {
                String columnName = getColumnName(entityFieldMap.get(fieldName));
                if (!columnMap.containsKey(columnName)) {
                    missingFields.add(MissingField.builder()
                            .fieldName(fieldName)
                            .missingFrom("database")
                            .presentIn("entity")
                            .entityName(entityClass.getSimpleName())
                            .tableName(tableName)
                            .build());
                }
            }
            
            // Columns missing in entity
            for (String columnName : columnMap.keySet()) {
                boolean foundInEntity = entityFieldMap.values().stream()
                        .anyMatch(field -> getColumnName(field).equals(columnName));
                
                if (!foundInEntity) {
                    missingFields.add(MissingField.builder()
                            .fieldName(columnName)
                            .missingFrom("entity")
                            .presentIn("database")
                            .entityName(entityClass.getSimpleName())
                            .tableName(tableName)
                            .build());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error finding missing fields for {}: {}", entityClass.getSimpleName(), e.getMessage());
        }
        
        return missingFields;
    }
    
    /**
     * Generate field mapping report
     */
    public void generateFieldMappingReport(List<Class<?>> entityClasses) {
        logger.info("=== Field Mapping Report ===");
        
        for (Class<?> entityClass : entityClasses) {
            logger.info("Entity: {}", entityClass.getSimpleName());
            
            Field[] fields = baseTypeValidator.getAllFields(entityClass);
            for (Field field : fields) {
                logger.info("  Field: {} (Type: {})", field.getName(), field.getType().getSimpleName());
            }
        }
    }
    
    // Private helper methods
    
    private void validateFieldMappings(Map<String, Field> entityFieldMap, Map<String, ColumnInfo> columnMap, 
                                     TypeValidationResult result) {
        for (Map.Entry<String, Field> entry : entityFieldMap.entrySet()) {
            Field field = entry.getValue();
            String columnName = getColumnName(field);
            ColumnInfo columnInfo = columnMap.get(columnName);
            
            if (columnInfo != null) {
                // Validate type compatibility
                if (!columnTypeMapper.areTypesCompatible(field.getType(), columnInfo)) {
                    Class<?> expectedType = columnTypeMapper.mapToJavaType(columnInfo);
                    
                    TypeMismatch mismatch = TypeMismatch.builder()
                            .fieldName(field.getName())
                            .expectedType(expectedType != null ? expectedType.getSimpleName() : "Unknown")
                            .actualType(field.getType().getSimpleName())
                            .layer("field-mapping")
                            .entityName(result.getEntityName())
                            .tableName(result.getTableName())
                            .severity(TypeMismatch.Severity.WARNING)
                            .suggestion(columnTypeMapper.generateTypeSuggestion(columnInfo, field.getType()))
                            .build();
                    
                    result.addMismatch(mismatch);
                }
            }
        }
    }
    
    private void findMissingFields(Map<String, Field> entityFieldMap, Map<String, ColumnInfo> columnMap, 
                                 TypeValidationResult result) {
        // Check for entity fields without corresponding database columns
        for (Map.Entry<String, Field> entry : entityFieldMap.entrySet()) {
            Field field = entry.getValue();
            String columnName = getColumnName(field);
            
            // Skip fields that don't map to database columns
            if (isNonDatabaseField(field)) {
                continue;
            }
            
            if (!columnMap.containsKey(columnName)) {
                result.addWarning(String.format("Entity field '%s' (column: %s) has no corresponding database column", 
                        field.getName(), columnName));
            }
        }
        
        // Check for database columns without corresponding entity fields
        for (String columnName : columnMap.keySet()) {
            boolean foundInEntity = entityFieldMap.values().stream()
                    .anyMatch(field -> getColumnName(field).equals(columnName));
            
            if (!foundInEntity) {
                result.addWarning(String.format("Database column '%s' has no corresponding entity field", columnName));
            }
        }
    }
    
    private Map<String, Field> getEntityFieldMap(Field[] fields) {
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : fields) {
            fieldMap.put(field.getName(), field);
        }
        return fieldMap;
    }
    
    private Map<String, ColumnInfo> getColumnMap(List<ColumnInfo> columns) {
        Map<String, ColumnInfo> columnMap = new HashMap<>();
        for (ColumnInfo column : columns) {
            columnMap.put(column.getColumnName(), column);
        }
        return columnMap;
    }
    
    private String getColumnName(Field field) {
        // Check @TableId annotation
        if (field.isAnnotationPresent(TableId.class)) {
            return "id"; // Primary key is typically 'id'
        }
        
        // Check @TableField annotation
        if (field.isAnnotationPresent(TableField.class)) {
            TableField tableField = field.getAnnotation(TableField.class);
            if (!tableField.value().isEmpty()) {
                return tableField.value();
            }
        }
        
        // Default to field name (convert camelCase to snake_case)
        return camelToSnakeCase(field.getName());
    }
    
    private boolean isNonDatabaseField(Field field) {
        // Check @TableField annotation with exist = false
        if (field.isAnnotationPresent(TableField.class)) {
            TableField tableField = field.getAnnotation(TableField.class);
            return !tableField.exist();
        }
        
        return false;
    }
    
    private String camelToSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
    
    private String getTableName(Class<?> entityClass) {
        TableName tableNameAnnotation = entityClass.getAnnotation(TableName.class);
        return tableNameAnnotation != null ? tableNameAnnotation.value() : null;
    }
    
    private TypeMismatch createValidationErrorMismatch(String entityName, String errorMessage) {
        return TypeMismatch.builder()
                .fieldName("validation")
                .expectedType("success")
                .actualType("error")
                .layer("field-mapping")
                .entityName(entityName)
                .severity(TypeMismatch.Severity.CRITICAL)
                .suggestion("Check entity configuration and database connectivity: " + errorMessage)
                .build();
    }
    
    /**
     * Get field names from entity fields
     */
    private Set<String> getEntityFieldNames(Field[] fields) {
        return java.util.Arrays.stream(fields)
                .map(Field::getName)
                .collect(Collectors.toSet());
    }
}