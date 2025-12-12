package com.xiuxian.game.validation;

/**
 * Represents database column information
 * Used by SchemaAnalyzer to store column metadata
 */
public class ColumnInfo {
    
    private String tableName;
    private String columnName;
    private String dataType;
    private int columnSize;
    private int decimalDigits;
    private boolean nullable;
    private boolean primaryKey;
    private boolean autoIncrement;
    private String defaultValue;
    private String remarks;
    
    // Constructors
    public ColumnInfo() {}
    
    public ColumnInfo(String tableName, String columnName, String dataType) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.dataType = dataType;
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
    
    public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    public int getColumnSize() {
        return columnSize;
    }
    
    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }
    
    public int getDecimalDigits() {
        return decimalDigits;
    }
    
    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }
    
    public boolean isNullable() {
        return nullable;
    }
    
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }
    
    public boolean isPrimaryKey() {
        return primaryKey;
    }
    
    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
    
    public boolean isAutoIncrement() {
        return autoIncrement;
    }
    
    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    /**
     * Get the full type description including size and precision
     */
    public String getFullTypeDescription() {
        StringBuilder sb = new StringBuilder(dataType);
        
        if (columnSize > 0) {
            sb.append("(").append(columnSize);
            if (decimalDigits > 0) {
                sb.append(",").append(decimalDigits);
            }
            sb.append(")");
        }
        
        return sb.toString();
    }
    
    /**
     * Check if this is a numeric type
     */
    public boolean isNumericType() {
        String type = dataType.toLowerCase();
        return type.contains("int") || type.contains("decimal") || 
               type.contains("numeric") || type.contains("float") || 
               type.contains("double");
    }
    
    /**
     * Check if this is a string type
     */
    public boolean isStringType() {
        String type = dataType.toLowerCase();
        return type.contains("varchar") || type.contains("char") || 
               type.contains("text");
    }
    
    /**
     * Check if this is a boolean type
     */
    public boolean isBooleanType() {
        String type = dataType.toLowerCase();
        return type.equals("tinyint") && columnSize == 1;
    }
    
    /**
     * Check if this is a date/time type
     */
    public boolean isDateTimeType() {
        String type = dataType.toLowerCase();
        return type.contains("timestamp") || type.contains("datetime") || 
               type.contains("date") || type.contains("time");
    }
    
    @Override
    public String toString() {
        return String.format("ColumnInfo{table='%s', column='%s', type='%s', size=%d, nullable=%s, pk=%s}", 
                tableName, columnName, getFullTypeDescription(), columnSize, nullable, primaryKey);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ColumnInfo that = (ColumnInfo) o;
        
        if (!tableName.equals(that.tableName)) return false;
        return columnName.equals(that.columnName);
    }
    
    @Override
    public int hashCode() {
        int result = tableName.hashCode();
        result = 31 * result + columnName.hashCode();
        return result;
    }
}