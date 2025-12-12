package com.xiuxian.game.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents complete schema information for a database table
 * Contains columns, foreign keys, and other table metadata
 */
public class TableSchema {
    
    private String tableName;
    private List<ColumnInfo> columns;
    private List<ForeignKeyInfo> foreignKeys;
    
    // Constructors
    public TableSchema() {}
    
    public TableSchema(String tableName, List<ColumnInfo> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }
    
    // Getters and Setters
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public List<ColumnInfo> getColumns() {
        return columns;
    }
    
    public void setColumns(List<ColumnInfo> columns) {
        this.columns = columns;
    }
    
    public List<ForeignKeyInfo> getForeignKeys() {
        return foreignKeys;
    }
    
    public void setForeignKeys(List<ForeignKeyInfo> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }
    
    // Utility methods
    
    /**
     * Find a column by name
     */
    public Optional<ColumnInfo> findColumn(String columnName) {
        if (columns == null) {
            return Optional.empty();
        }
        
        return columns.stream()
                .filter(col -> col.getColumnName().equalsIgnoreCase(columnName))
                .findFirst();
    }
    
    /**
     * Get all primary key columns
     */
    public List<ColumnInfo> getPrimaryKeyColumns() {
        if (columns == null) {
            return new ArrayList<>();
        }
        
        return columns.stream()
                .filter(ColumnInfo::isPrimaryKey)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get all foreign key columns
     */
    public List<ColumnInfo> getForeignKeyColumns() {
        if (columns == null || foreignKeys == null) {
            return new ArrayList<>();
        }
        
        return columns.stream()
                .filter(col -> foreignKeys.stream()
                        .anyMatch(fk -> fk.getColumnName().equals(col.getColumnName())))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Check if a column exists
     */
    public boolean hasColumn(String columnName) {
        return findColumn(columnName).isPresent();
    }
    
    /**
     * Get column count
     */
    public int getColumnCount() {
        return columns != null ? columns.size() : 0;
    }
    
    /**
     * Get foreign key count
     */
    public int getForeignKeyCount() {
        return foreignKeys != null ? foreignKeys.size() : 0;
    }
    
    @Override
    public String toString() {
        return String.format("TableSchema{table='%s', columns=%d, foreignKeys=%d}", 
                tableName, getColumnCount(), getForeignKeyCount());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TableSchema that = (TableSchema) o;
        
        return tableName.equals(that.tableName);
    }
    
    @Override
    public int hashCode() {
        return tableName.hashCode();
    }
}