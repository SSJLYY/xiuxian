package com.xiuxian.game.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * Service for analyzing database schema information
 * Extracts table structures, column types, and constraints from the database
 * Implements Requirements 4.1, 4.2, 2.2
 */
@Component
public class SchemaAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaAnalyzer.class);
    
    @Autowired
    private DataSource dataSource;
    
    /**
     * Get all table names from the database
     */
    public List<String> getAllTableNames() {
        List<String> tableNames = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            
            try (ResultSet tables = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    tableNames.add(tableName);
                }
            }
            
            logger.debug("Found {} tables in database", tableNames.size());
            
        } catch (SQLException e) {
            logger.error("Error retrieving table names: {}", e.getMessage(), e);
        }
        
        return tableNames;
    }
    
    /**
     * Get detailed column information for a specific table
     */
    public List<ColumnInfo> getTableColumns(String tableName) {
        List<ColumnInfo> columns = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            
            try (ResultSet columnsResult = metaData.getColumns(catalog, null, tableName, "%")) {
                while (columnsResult.next()) {
                    ColumnInfo column = new ColumnInfo();
                    column.setTableName(tableName);
                    column.setColumnName(columnsResult.getString("COLUMN_NAME"));
                    column.setDataType(columnsResult.getString("TYPE_NAME"));
                    column.setColumnSize(columnsResult.getInt("COLUMN_SIZE"));
                    column.setDecimalDigits(columnsResult.getInt("DECIMAL_DIGITS"));
                    column.setNullable(columnsResult.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    column.setAutoIncrement(columnsResult.getString("IS_AUTOINCREMENT").equals("YES"));
                    column.setDefaultValue(columnsResult.getString("COLUMN_DEF"));
                    column.setRemarks(columnsResult.getString("REMARKS"));
                    
                    columns.add(column);
                }
            }
            
            // Get primary key information
            Set<String> primaryKeys = getPrimaryKeys(connection, catalog, tableName);
            for (ColumnInfo column : columns) {
                column.setPrimaryKey(primaryKeys.contains(column.getColumnName()));
            }
            
            logger.debug("Found {} columns for table {}", columns.size(), tableName);
            
        } catch (SQLException e) {
            logger.error("Error retrieving columns for table {}: {}", tableName, e.getMessage(), e);
        }
        
        return columns;
    }
    
    /**
     * Get primary key columns for a table
     */
    private Set<String> getPrimaryKeys(Connection connection, String catalog, String tableName) throws SQLException {
        Set<String> primaryKeys = new HashSet<>();
        
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet primaryKeysResult = metaData.getPrimaryKeys(catalog, null, tableName)) {
            while (primaryKeysResult.next()) {
                primaryKeys.add(primaryKeysResult.getString("COLUMN_NAME"));
            }
        }
        
        return primaryKeys;
    }
    
    /**
     * Get foreign key information for a table
     */
    public List<ForeignKeyInfo> getForeignKeys(String tableName) {
        List<ForeignKeyInfo> foreignKeys = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            
            try (ResultSet foreignKeysResult = metaData.getImportedKeys(catalog, null, tableName)) {
                while (foreignKeysResult.next()) {
                    ForeignKeyInfo fk = new ForeignKeyInfo();
                    fk.setTableName(tableName);
                    fk.setColumnName(foreignKeysResult.getString("FKCOLUMN_NAME"));
                    fk.setReferencedTableName(foreignKeysResult.getString("PKTABLE_NAME"));
                    fk.setReferencedColumnName(foreignKeysResult.getString("PKCOLUMN_NAME"));
                    fk.setConstraintName(foreignKeysResult.getString("FK_NAME"));
                    
                    foreignKeys.add(fk);
                }
            }
            
            logger.debug("Found {} foreign keys for table {}", foreignKeys.size(), tableName);
            
        } catch (SQLException e) {
            logger.error("Error retrieving foreign keys for table {}: {}", tableName, e.getMessage(), e);
        }
        
        return foreignKeys;
    }
    
    /**
     * Get complete schema information for all tables
     */
    public Map<String, TableSchema> getCompleteSchema() {
        Map<String, TableSchema> schema = new HashMap<>();
        
        List<String> tableNames = getAllTableNames();
        
        for (String tableName : tableNames) {
            TableSchema tableSchema = new TableSchema();
            tableSchema.setTableName(tableName);
            tableSchema.setColumns(getTableColumns(tableName));
            tableSchema.setForeignKeys(getForeignKeys(tableName));
            
            schema.put(tableName, tableSchema);
        }
        
        logger.info("Analyzed complete schema for {} tables", schema.size());
        
        return schema;
    }
    
    /**
     * Check if a table exists in the database
     */
    public boolean tableExists(String tableName) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            
            try (ResultSet tables = metaData.getTables(catalog, null, tableName, new String[]{"TABLE"})) {
                return tables.next();
            }
            
        } catch (SQLException e) {
            logger.error("Error checking if table {} exists: {}", tableName, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get database metadata information
     */
    public DatabaseInfo getDatabaseInfo() {
        DatabaseInfo dbInfo = new DatabaseInfo();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            dbInfo.setDatabaseProductName(metaData.getDatabaseProductName());
            dbInfo.setDatabaseProductVersion(metaData.getDatabaseProductVersion());
            dbInfo.setDriverName(metaData.getDriverName());
            dbInfo.setDriverVersion(metaData.getDriverVersion());
            dbInfo.setCatalogName(connection.getCatalog());
            dbInfo.setSchemaName(connection.getSchema());
            
            logger.debug("Database info: {} {}", dbInfo.getDatabaseProductName(), dbInfo.getDatabaseProductVersion());
            
        } catch (SQLException e) {
            logger.error("Error retrieving database info: {}", e.getMessage(), e);
        }
        
        return dbInfo;
    }
}