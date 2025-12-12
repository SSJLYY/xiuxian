package com.xiuxian.game.validation;

/**
 * Represents database metadata information
 * Used by SchemaAnalyzer to store database connection details
 */
public class DatabaseInfo {
    
    private String databaseProductName;
    private String databaseProductVersion;
    private String driverName;
    private String driverVersion;
    private String catalogName;
    private String schemaName;
    
    // Constructors
    public DatabaseInfo() {}
    
    // Getters and Setters
    public String getDatabaseProductName() {
        return databaseProductName;
    }
    
    public void setDatabaseProductName(String databaseProductName) {
        this.databaseProductName = databaseProductName;
    }
    
    public String getDatabaseProductVersion() {
        return databaseProductVersion;
    }
    
    public void setDatabaseProductVersion(String databaseProductVersion) {
        this.databaseProductVersion = databaseProductVersion;
    }
    
    public String getDriverName() {
        return driverName;
    }
    
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
    
    public String getDriverVersion() {
        return driverVersion;
    }
    
    public void setDriverVersion(String driverVersion) {
        this.driverVersion = driverVersion;
    }
    
    public String getCatalogName() {
        return catalogName;
    }
    
    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
    
    public String getSchemaName() {
        return schemaName;
    }
    
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
    
    @Override
    public String toString() {
        return String.format("DatabaseInfo{product='%s %s', driver='%s %s', catalog='%s', schema='%s'}", 
                databaseProductName, databaseProductVersion, driverName, driverVersion, catalogName, schemaName);
    }
}