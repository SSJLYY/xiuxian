package com.xiuxian.game.validation;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Core service for validating data consistency across layers
 * Validates entity-database type consistency and field mappings
 */
@Service
public class DataConsistencyValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(DataConsistencyValidator.class);
    
    @Autowired
    private BaseTypeValidator baseTypeValidator;
    
    @Autowired
    private SchemaAnalyzer schemaAnalyzer;
    
    @Autowired
    private ColumnTypeMapper columnTypeMapper;
    
    /**
     * Validate entity consistency with database schema
     * This implements Property 8: Runtime Validation
     */
    public TypeValidationResult validateEntityConsistency(Class<?> entityClass) {
        logger.info("Validating entity consistency for: {}", entityClass.getSimpleName());
        
        TypeValidationResult result = TypeValidationResult.builder()
                .entityName(entityClass.getSimpleName())
                .build();
        
        try {
            // Get table name from @TableName annotation
            String tableName = getTableName(entityClass);
            if (tableName == null) {
                result.addWarning("No @TableName annotation found for entity: " + entityClass.getSimpleName());
                return result;
            }
            
            result.setTableName(tableName);
            
            // Get database schema information using SchemaAnalyzer
            List<ColumnInfo> columns = schemaAnalyzer.getTableColumns(tableName);
            if (columns.isEmpty()) {
                result.addWarning("No database columns found for table: " + tableName);
                return result;
            }
            
            // Validate each entity field
            Field[] fields = baseTypeValidator.getAllFields(entityClass);
            for (Field field : fields) {
                validateEntityField(field, columns, result);
            }
            
        } catch (Exception e) {
            logger.error("Error validating entity consistency for {}: {}", entityClass.getSimpleName(), e.getMessage());
            result.addMismatch(TypeMismatch.builder()
                    .fieldName("validation")
                    .expectedType("success")
                    .actualType("error")
                    .layer("validation-framework")
                    .entityName(entityClass.getSimpleName())
                    .severity(TypeMismatch.Severity.CRITICAL)
                    .suggestion("Check database connection and entity annotations")
                    .build());
        }
        
        baseTypeValidator.logValidationResult(result);
        return result;
    }
    
    /**
     * Validate all entities in a package
     */
    public List<TypeValidationResult> validateAllEntities(List<Class<?>> entityClasses) {
        List<TypeValidationResult> results = new ArrayList<>();
        
        for (Class<?> entityClass : entityClasses) {
            TypeValidationResult result = validateEntityConsistency(entityClass);
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * Get table name from @TableName annotation
     */
    private String getTableName(Class<?> entityClass) {
        TableName tableNameAnnotation = entityClass.getAnnotation(TableName.class);
        if (tableNameAnnotation != null) {
            return tableNameAnnotation.value();
        }
        return null;
    }
    
    /**
     * Find database column information by name
     */
    private Optional<ColumnInfo> findColumnInfo(List<ColumnInfo> columns, String columnName) {
        return columns.stream()
                .filter(col -> col.getColumnName().equalsIgnoreCase(columnName))
                .findFirst();
    }
    
    /**
     * Validate a single entity field against database schema
     */
    private void validateEntityField(Field field, List<ColumnInfo> columns, TypeValidationResult result) {
        // Skip fields that don't map to database columns
        if (field.isAnnotationPresent(TableField.class)) {
            TableField tableField = field.getAnnotation(TableField.class);
            if (!tableField.exist()) {
                return; // Skip non-database fields
            }
        }
        
        String columnName = getColumnName(field);
        Optional<ColumnInfo> columnInfoOpt = findColumnInfo(columns, columnName);
        
        if (!columnInfoOpt.isPresent()) {
            result.addWarning(String.format("No database column found for field: %s (column: %s)", 
                    field.getName(), columnName));
            return;
        }
        
        ColumnInfo columnInfo = columnInfoOpt.get();
        
        // Check type compatibility using ColumnTypeMapper
        Class<?> fieldType = field.getType();
        if (!columnTypeMapper.areTypesCompatible(fieldType, columnInfo)) {
            Class<?> expectedType = columnTypeMapper.mapToJavaType(columnInfo);
            
            TypeMismatch.Severity severity = determineSeverity(fieldType, expectedType);
            
            String suggestion = columnTypeMapper.generateTypeSuggestion(columnInfo, fieldType);
            
            TypeMismatch mismatch = TypeMismatch.builder()
                    .fieldName(field.getName())
                    .expectedType(expectedType != null ? expectedType.getSimpleName() : "Unknown")
                    .actualType(fieldType.getSimpleName())
                    .layer("backend-database")
                    .entityName(result.getEntityName())
                    .tableName(result.getTableName())
                    .severity(severity)
                    .suggestion(suggestion)
                    .build();
            
            result.addMismatch(mismatch);
        }
    }
    
    /**
     * Get column name for a field
     */
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
        
        // Default to field name
        return field.getName();
    }
    
    /**
     * Determine severity of type mismatch
     */
    private TypeMismatch.Severity determineSeverity(Class<?> actualType, Class<?> expectedType) {
        if (expectedType == null) {
            return TypeMismatch.Severity.WARNING;
        }
        
        // Critical: Long vs Integer mismatches can cause runtime errors
        if ((actualType == Long.class && expectedType == Integer.class) ||
            (actualType == Integer.class && expectedType == Long.class)) {
            return TypeMismatch.Severity.CRITICAL;
        }
        
        // Warning: Other type mismatches
        return TypeMismatch.Severity.WARNING;
    }
}