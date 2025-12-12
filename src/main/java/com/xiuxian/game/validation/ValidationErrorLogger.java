package com.xiuxian.game.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service for comprehensive error logging of validation issues
 * Provides detailed error messages and standardized logging format
 * Implements Requirements 2.4, 3.4, 3.5
 */
@Service
public class ValidationErrorLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationErrorLogger.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Log field mapping inconsistencies with detailed information
     * Property 7: Error Logging Detail
     */
    public void logFieldMappingInconsistencies(List<TypeValidationResult> validationResults) {
        logger.info("=== Field Mapping Inconsistency Report ===");
        logger.info("Timestamp: {}", LocalDateTime.now().format(TIMESTAMP_FORMAT));
        
        int totalEntities = validationResults.size();
        int entitiesWithIssues = 0;
        int totalMismatches = 0;
        
        for (TypeValidationResult result : validationResults) {
            if (!result.isValid() || result.hasWarnings()) {
                entitiesWithIssues++;
                
                logger.error("Entity: {} (Table: {})", result.getEntityName(), result.getTableName());
                
                // Log type mismatches
                for (TypeMismatch mismatch : result.getMismatches()) {
                    totalMismatches++;
                    logTypeMismatchDetails(mismatch);
                }
                
                // Log warnings
                for (String warning : result.getWarnings()) {
                    logger.warn("  WARNING: {}", warning);
                }
            }
        }
        
        // Log summary
        logger.info("=== Summary ===");
        logger.info("Total entities analyzed: {}", totalEntities);
        logger.info("Entities with issues: {}", entitiesWithIssues);
        logger.info("Total type mismatches: {}", totalMismatches);
        
        if (totalMismatches == 0) {
            logger.info("SUCCESS: No field mapping inconsistencies found");
        } else {
            logger.error("ERROR: {} field mapping inconsistencies require attention", totalMismatches);
        }
    }
    
    /**
     * Log validation success confirmations
     * Property 9: Validation Success Confirmation
     */
    public void logValidationSuccess(ValidationSummary summary) {
        logger.info("=== Validation Success Confirmation ===");
        logger.info("Timestamp: {}", LocalDateTime.now().format(TIMESTAMP_FORMAT));
        
        if (summary.isSuccessful()) {
            logger.info("SUCCESS: All data types are consistent across layers");
            logger.info("SUCCESS: Total entities validated: {}", summary.getTotalEntities());
            logger.info("SUCCESS: All {} entities passed validation", summary.getValidEntities());
            logger.info("SUCCESS: No critical type mismatches detected");
            logger.info("SUCCESS: System is ready for operation");
        } else {
            logger.warn("WARNING: Validation completed with issues:");
            logger.warn("  - Entities with errors: {}", summary.getEntitiesWithErrors());
            logger.warn("  - Critical mismatches: {}", summary.getCriticalMismatches());
            logger.warn("  - Warning mismatches: {}", summary.getWarningMismatches());
        }
    }
    
    /**
     * Log missing field details
     */
    public void logMissingFieldDetails(List<MissingField> missingFields) {
        if (missingFields.isEmpty()) {
            logger.info("SUCCESS: No missing fields detected");
            return;
        }
        
        logger.error("=== Missing Field Details ===");
        logger.error("Timestamp: {}", LocalDateTime.now().format(TIMESTAMP_FORMAT));
        
        Map<String, List<MissingField>> groupedByEntity = missingFields.stream()
                .collect(java.util.stream.Collectors.groupingBy(MissingField::getEntityName));
        
        for (Map.Entry<String, List<MissingField>> entry : groupedByEntity.entrySet()) {
            String entityName = entry.getKey();
            List<MissingField> entityMissingFields = entry.getValue();
            
            logger.error("Entity: {}", entityName);
            
            for (MissingField missingField : entityMissingFields) {
                logger.error("  MISSING FIELD: {}", missingField.getFieldName());
                logger.error("    Missing from: {}", missingField.getMissingFrom());
                logger.error("    Present in: {}", missingField.getPresentIn());
                logger.error("    Field type: {}", missingField.getFieldType());
                logger.error("    Table: {}", missingField.getTableName());
                
                if (missingField.getSuggestion() != null) {
                    logger.error("    Suggestion: {}", missingField.getSuggestion());
                }
            }
        }
        
        logger.error("Total missing fields: {}", missingFields.size());
    }
    
    /**
     * Log API response validation errors
     */
    public void logAPIResponseValidationErrors(APIResponseValidationReport report) {
        logger.info("=== API Response Validation Report ===");
        logger.info("Timestamp: {}", LocalDateTime.now().format(TIMESTAMP_FORMAT));
        
        APIResponseValidationSummary summary = report.getSummary();
        
        logger.info("Total API responses analyzed: {}", summary.getTotalResponses());
        logger.info("Valid responses: {}", summary.getValidResponses());
        logger.info("Responses with issues: {}", summary.getResponsesWithIssues());
        
        if (summary.hasAnyIssues()) {
            logger.error("API Response validation issues detected:");
            
            for (APIResponseValidationResult result : report.getResultsWithIssues()) {
                logger.error("Response Class: {}", result.getResponseClassName());
                
                for (APIFieldValidation fieldValidation : result.getInvalidFieldValidations()) {
                    logger.error("  INVALID FIELD: {}", fieldValidation.getFieldName());
                    logger.error("    Type: {}", fieldValidation.getActualTypeName());
                    logger.error("    Present: {}", fieldValidation.isPresent());
                    logger.error("    Valid: {}", fieldValidation.isValid());
                    
                    if (fieldValidation.getIssue() != null) {
                        logger.error("    Issue: {}", fieldValidation.getIssue());
                    }
                    
                    if (fieldValidation.getSuggestion() != null) {
                        logger.error("    Suggestion: {}", fieldValidation.getSuggestion());
                    }
                }
            }
        } else {
            logger.info("SUCCESS: All API responses are valid");
        }
    }
    
    /**
     * Log type standardization issues
     */
    public void logTypeStandardizationIssues(TypeStandardizationReport report) {
        logger.info("=== Type Standardization Report ===");
        logger.info("Timestamp: {}", LocalDateTime.now().format(TIMESTAMP_FORMAT));
        
        TypeStandardizationSummary summary = report.getSummary();
        
        logger.info("Total entities analyzed: {}", summary.getTotalEntities());
        logger.info("Valid entities: {}", summary.getValidEntities());
        logger.info("Total recommendations: {}", summary.getTotalRecommendations());
        
        if (report.hasCriticalIssues()) {
            logger.error("Critical type standardization issues detected:");
            
            List<TypeStandardizationRecommendation> highPriorityRecommendations = report.getHighPriorityRecommendations();
            
            for (TypeStandardizationRecommendation recommendation : highPriorityRecommendations) {
                logger.error("HIGH PRIORITY: Table {}, Column {}", 
                        recommendation.getTableName(), recommendation.getColumnName());
                logger.error("  Current type: {}", recommendation.getCurrentType().getSimpleName());
                logger.error("  Recommended type: {}", recommendation.getRecommendedType().getSimpleName());
                logger.error("  Reason: {}", recommendation.getReason());
                logger.error("  Priority: {}", recommendation.getPriority());
            }
        } else if (report.hasAnyIssues()) {
            logger.warn("Type standardization recommendations available - see detailed logs");
        } else {
            logger.info("SUCCESS: All types follow standardization guidelines");
        }
    }
    
    /**
     * Log startup validation results
     */
    public void logStartupValidationResults(List<TypeValidationResult> results, boolean failOnCritical) {
        logger.info("=== Startup Validation Results ===");
        logger.info("Timestamp: {}", LocalDateTime.now().format(TIMESTAMP_FORMAT));
        
        ValidationSummary summary = ValidationUtils.generateSummary(results);
        
        logger.info("Validation configuration:");
        logger.info("  Fail on critical mismatches: {}", failOnCritical);
        
        if (summary.isSuccessful()) {
            logValidationSuccess(summary);
        } else {
            logger.error("Startup validation detected issues:");
            logger.error("  Critical mismatches: {}", summary.getCriticalMismatches());
            logger.error("  Warning mismatches: {}", summary.getWarningMismatches());
            logger.error("  Entities with errors: {}", summary.getEntitiesWithErrors());
            
            if (summary.getCriticalMismatches() > 0 && failOnCritical) {
                logger.error("ERROR: Application startup will be terminated due to critical validation failures");
            } else if (summary.getCriticalMismatches() > 0) {
                logger.warn("WARNING: Critical validation issues detected but startup will continue");
            }
        }
    }
    
    // Private helper methods
    
    private void logTypeMismatchDetails(TypeMismatch mismatch) {
        String severity = mismatch.getSeverity().toString();
        String prefix = mismatch.getSeverity() == TypeMismatch.Severity.CRITICAL ? "CRITICAL" : "WARNING";
        
        logger.error("  {}: Field '{}' type mismatch", prefix, mismatch.getFieldName());
        logger.error("    Expected type: {}", mismatch.getExpectedType());
        logger.error("    Actual type: {}", mismatch.getActualType());
        logger.error("    Layer: {}", mismatch.getLayer());
        logger.error("    Entity: {}", mismatch.getEntityName());
        
        if (mismatch.getTableName() != null) {
            logger.error("    Table: {}", mismatch.getTableName());
        }
        
        if (mismatch.getSuggestion() != null) {
            logger.error("    Suggestion: {}", mismatch.getSuggestion());
        }
        
        logger.error("    Severity: {}", severity);
    }
}