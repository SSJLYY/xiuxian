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

/**
 * Service for detecting missing fields across layers
 * Identifies fields that exist in one layer but not in others
 * Implements Requirements 2.1, 2.3, 1.1
 */
@Service
public class MissingFieldDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(MissingFieldDetector.class);
    
    @Autowired
    private SchemaAnalyzer schemaAnalyzer;
    
    /**
     * Detect missing fields between entity and database
     */
    public List<MissingField> detectEntityDatabaseMissingFields(Class<?> entityClass) {
        List<MissingField> missingFields = new ArrayList<>();
        
        try {
            String tableName = getTableName(entityClass);
            if (tableName == null) {
                logger.warn("No @TableName annotation found for entity: {}", entityClass.getSimpleName());
                return missingFields;
            }
            
            List<ColumnInfo> columns = schemaAnalyzer.getTableColumns(tableName);
            Field[] entityFields = entityClass.getDeclaredFields();
            
            // Create maps for easier lookup
            Map<String, ColumnInfo> columnMap = createColumnMap(columns);
            
            // Find entity fields without corresponding database columns
            for (Field field : entityFields) {
                if (isNonDatabaseField(field)) {
                    continue; // Skip fields that don't map to database
                }
                
                String columnName = getColumnName(field);
                if (!columnMap.containsKey(columnName)) {
                    missingFields.add(MissingField.builder()
                            .fieldName(field.getName())
                            .missingFrom("database")
                            .presentIn("entity")
                            .entityName(entityClass.getSimpleName())
                            .tableName(tableName)
                            .fieldType(field.getType().getSimpleName())
                            .suggestion(String.format("Add column '%s' to table '%s' or mark field as non-database", 
                                    columnName, tableName))
                            .build());
                }
            }
            
            // Find database columns without corresponding entity fields
            for (ColumnInfo column : columns) {
                boolean foundInEntity = Arrays.stream(entityFields)
                        .anyMatch(field -> getColumnName(field).equals(column.getColumnName()));
                
                if (!foundInEntity) {
                    missingFields.add(MissingField.builder()
                            .fieldName(column.getColumnName())
                            .missingFrom("entity")
                            .presentIn("database")
                            .entityName(entityClass.getSimpleName())
                            .tableName(tableName)
                            .fieldType(column.getDataType())
                            .suggestion(String.format("Add field for column '%s' to entity '%s'", 
                                    column.getColumnName(), entityClass.getSimpleName()))
                            .build());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error detecting missing fields for entity {}: {}", entityClass.getSimpleName(), e.getMessage());
        }
        
        return missingFields;
    }
    
    /**
     * Detect missing fields between DTO and entity
     */
    public List<MissingField> detectDTOEntityMissingFields(Class<?> dtoClass, Class<?> entityClass) {
        List<MissingField> missingFields = new ArrayList<>();
        
        try {
            Field[] dtoFields = dtoClass.getDeclaredFields();
            Field[] entityFields = entityClass.getDeclaredFields();
            
            Map<String, Field> dtoFieldMap = createFieldMap(dtoFields);
            Map<String, Field> entityFieldMap = createFieldMap(entityFields);
            
            // Find DTO fields without corresponding entity fields
            for (Field dtoField : dtoFields) {
                if (!entityFieldMap.containsKey(dtoField.getName())) {
                    missingFields.add(MissingField.builder()
                            .fieldName(dtoField.getName())
                            .missingFrom("entity")
                            .presentIn("dto")
                            .entityName(entityClass.getSimpleName())
                            .dtoName(dtoClass.getSimpleName())
                            .fieldType(dtoField.getType().getSimpleName())
                            .suggestion(String.format("Add field '%s' to entity '%s' or remove from DTO", 
                                    dtoField.getName(), entityClass.getSimpleName()))
                            .build());
                }
            }
            
            // Find entity fields without corresponding DTO fields
            for (Field entityField : entityFields) {
                if (isNonDatabaseField(entityField)) {
                    continue; // Skip non-database fields
                }
                
                if (!dtoFieldMap.containsKey(entityField.getName())) {
                    missingFields.add(MissingField.builder()
                            .fieldName(entityField.getName())
                            .missingFrom("dto")
                            .presentIn("entity")
                            .entityName(entityClass.getSimpleName())
                            .dtoName(dtoClass.getSimpleName())
                            .fieldType(entityField.getType().getSimpleName())
                            .suggestion(String.format("Add field '%s' to DTO '%s' if needed for API", 
                                    entityField.getName(), dtoClass.getSimpleName()))
                            .build());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error detecting missing fields between DTO {} and entity {}: {}", 
                    dtoClass.getSimpleName(), entityClass.getSimpleName(), e.getMessage());
        }
        
        return missingFields;
    }
    
    /**
     * Generate comprehensive missing field report
     */
    public MissingFieldReport generateMissingFieldReport(List<Class<?>> entityClasses, List<Class<?>> dtoClasses) {
        MissingFieldReport report = new MissingFieldReport();
        
        // Check entity-database missing fields
        for (Class<?> entityClass : entityClasses) {
            List<MissingField> missingFields = detectEntityDatabaseMissingFields(entityClass);
            report.addEntityDatabaseMissingFields(entityClass.getSimpleName(), missingFields);
        }
        
        // Check DTO-entity missing fields (if DTOs are provided)
        if (dtoClasses != null && !dtoClasses.isEmpty()) {
            for (Class<?> dtoClass : dtoClasses) {
                // Try to find corresponding entity class
                Class<?> correspondingEntity = findCorrespondingEntity(dtoClass, entityClasses);
                if (correspondingEntity != null) {
                    List<MissingField> missingFields = detectDTOEntityMissingFields(dtoClass, correspondingEntity);
                    report.addDTOEntityMissingFields(dtoClass.getSimpleName(), missingFields);
                }
            }
        }
        
        return report;
    }
    
    /**
     * Get summary of missing fields by severity
     */
    public MissingFieldSummary getMissingFieldSummary(List<MissingField> missingFields) {
        MissingFieldSummary summary = new MissingFieldSummary();
        
        for (MissingField missingField : missingFields) {
            summary.incrementTotal();
            
            if ("database".equals(missingField.getMissingFrom())) {
                summary.incrementMissingFromDatabase();
            } else if ("entity".equals(missingField.getMissingFrom())) {
                summary.incrementMissingFromEntity();
            } else if ("dto".equals(missingField.getMissingFrom())) {
                summary.incrementMissingFromDTO();
            }
        }
        
        return summary;
    }
    
    // Private helper methods
    
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
        // Check @TableId annotation
        if (field.isAnnotationPresent(TableId.class)) {
            return "id";
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
    
    private Class<?> findCorrespondingEntity(Class<?> dtoClass, List<Class<?>> entityClasses) {
        String dtoName = dtoClass.getSimpleName();
        
        // Try to match by naming convention (e.g., PlayerResponse -> Player, PlayerRequest -> Player)
        String baseName = dtoName.replaceAll("(Request|Response|DTO)$", "");
        
        for (Class<?> entityClass : entityClasses) {
            if (entityClass.getSimpleName().equals(baseName) || 
                entityClass.getSimpleName().equals(baseName + "Profile") ||
                entityClass.getSimpleName().equals("Player" + baseName)) {
                return entityClass;
            }
        }
        
        return null;
    }
}
