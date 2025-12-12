package com.xiuxian.game.validation;

/**
 * Represents foreign key constraint information
 * Used by SchemaAnalyzer to store foreign key metadata
 */
public class ForeignKeyInfo {
    
    private String tableName;
    private String columnName;
    private String referencedTableName;
    private String referencedColumnName;
    private String constraintName;
    
    // Constructors
    public ForeignKeyInfo() {}
    
    public ForeignKeyInfo(String tableName, String columnName, String referencedTableName, String referencedColumnName) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.referencedTableName = referencedTableName;
        this.referencedColumnName = referencedColumnName;
    }
    
    // Getters and Setters
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    
    public String getReferencedTableName() {
        return referencedTableName;
    }
    
    public void setReferencedTableName(String referencedTableName) {
        this.referencedTableName = referencedTableName;
    }
    
    public String getReferencedColumnName() {
        return referencedColumnName;
    }
    
    public void setReferencedColumnName(String referencedColumnName) {
        this.referencedColumnName = referencedColumnName;
    }
    
    public String getConstraintName() {
        return constraintName;
    }
    
    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }
    
    @Override
    public String toString() {
        return String.format("ForeignKey{%s.%s -> %s.%s (constraint: %s)}", 
                tableName, columnName, referencedTableName, referencedColumnName, constraintName);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ForeignKeyInfo that = (ForeignKeyInfo) o;
        
        if (!tableName.equals(that.tableName)) return false;
        if (!columnName.equals(that.columnName)) return false;
        if (!referencedTableName.equals(that.referencedTableName)) return false;
        return referencedColumnName.equals(that.referencedColumnName);
    }
    
    @Override
    public int hashCode() {
        int result = tableName.hashCode();
        result = 31 * result + columnName.hashCode();
        result = 31 * result + referencedTableName.hashCode();
        result = 31 * result + referencedColumnName.hashCode();
        return result;
    }
}