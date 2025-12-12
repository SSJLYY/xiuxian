package com.xiuxian.game.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a missing field between layers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissingField {
    
    private String fieldName;
    private String missingFrom; // "database", "entity", "dto"
    private String presentIn;   // "database", "entity", "dto"
    private String entityName;
    private String tableName;
    private String dtoName;
    private String fieldType;
    private String suggestion;
    
    @Override
    public String toString() {
        return String.format("Field '%s' is missing from %s but present in %s (Entity: %s, Table: %s)", 
                fieldName, missingFrom, presentIn, entityName, tableName);
    }
}