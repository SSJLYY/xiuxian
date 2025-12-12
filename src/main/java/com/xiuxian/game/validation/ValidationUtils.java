package com.xiuxian.game.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for validation operations
 * Provides helper methods for validation reporting and analysis
 */
public class ValidationUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtils.class);
    
    /**
     * Generate a summary report of validation results
     */
    public static ValidationSummary generateSummary(List<TypeValidationResult> results) {
        ValidationSummary summary = new ValidationSummary();
        
        summary.setTotalEntities(results.size());
        summary.setValidEntities((int) results.stream().filter(TypeValidationResult::isValid).count());
        summary.setEntitiesWithWarnings((int) results.stream().filter(TypeValidationResult::hasWarnings).count());
        summary.setEntitiesWithErrors((int) results.stream().filter(r -> !r.isValid()).count());
        
        // Count total mismatches by severity
        long criticalMismatches = results.stream()
                .flatMap(r -> r.getMismatches().stream())
                .filter(m -> m.getSeverity() == TypeMismatch.Severity.CRITICAL)
                .count();
        
        long warningMismatches = results.stream()
                .flatMap(r -> r.getMismatches().stream())
                .filter(m -> m.getSeverity() == TypeMismatch.Severity.WARNING)
                .count();
        
        summary.setCriticalMismatches((int) criticalMismatches);
        summary.setWarningMismatches((int) warningMismatches);
        
        return summary;
    }
    
    /**
     * Filter results to only include those with critical errors
     */
    public static List<TypeValidationResult> getCriticalErrors(List<TypeValidationResult> results) {
        return results.stream()
                .filter(result -> result.getMismatches().stream()
                        .anyMatch(mismatch -> mismatch.getSeverity() == TypeMismatch.Severity.CRITICAL))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all critical mismatches from results
     */
    public static List<TypeMismatch> getAllCriticalMismatches(List<TypeValidationResult> results) {
        return results.stream()
                .flatMap(result -> result.getMismatches().stream())
                .filter(mismatch -> mismatch.getSeverity() == TypeMismatch.Severity.CRITICAL)
                .collect(Collectors.toList());
    }
    
    /**
     * Log validation summary
     */
    public static void logSummary(ValidationSummary summary) {
        logger.info("=== Validation Summary ===");
        logger.info("Total entities: {}", summary.getTotalEntities());
        logger.info("Valid entities: {}", summary.getValidEntities());
        logger.info("Entities with warnings: {}", summary.getEntitiesWithWarnings());
        logger.info("Entities with errors: {}", summary.getEntitiesWithErrors());
        logger.info("Critical mismatches: {}", summary.getCriticalMismatches());
        logger.info("Warning mismatches: {}", summary.getWarningMismatches());
        
        if (summary.getCriticalMismatches() == 0 && summary.getEntitiesWithErrors() == 0) {
            logger.info("SUCCESS: All validations passed successfully");
        } else if (summary.getCriticalMismatches() > 0) {
            logger.error("ERROR: Critical validation errors found");
        } else {
            logger.warn("WARNING: Validation completed with warnings");
        }
    }
    
    /**
     * Check if validation results indicate success
     */
    public static boolean isValidationSuccessful(List<TypeValidationResult> results) {
        return results.stream().allMatch(TypeValidationResult::isValid);
    }
    
    /**
     * Check if validation results have critical errors
     */
    public static boolean hasCriticalErrors(List<TypeValidationResult> results) {
        return results.stream()
                .anyMatch(result -> result.getMismatches().stream()
                        .anyMatch(mismatch -> mismatch.getSeverity() == TypeMismatch.Severity.CRITICAL));
    }
}