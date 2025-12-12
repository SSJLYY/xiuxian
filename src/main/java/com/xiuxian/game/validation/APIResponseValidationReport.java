package com.xiuxian.game.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive report for API response validation across multiple response classes
 * Provides aggregate statistics and summary information
 */
public class APIResponseValidationReport {
    
    private List<APIResponseValidationResult> validationResults;
    private APIResponseValidationSummary summary;
    
    public APIResponseValidationReport() {
        this.validationResults = new ArrayList<>();
        this.summary = new APIResponseValidationSummary();
    }
    
    // Methods to add data
    public void addValidationResult(APIResponseValidationResult result) {
        validationResults.add(result);
        updateSummary(result);
    }
    
    // Getters
    public List<APIResponseValidationResult> getValidationResults() {
        return validationResults;
    }
    
    public APIResponseValidationSummary getSummary() {
        return summary;
    }
    
    // Analysis methods
    public List<APIResponseValidationResult> getResultsWithIssues() {
        return validationResults.stream().filter(APIResponseValidationResult::hasAnyIssues).collect(java.util.stream.Collectors.toList());
    }
    
    public List<APIResponseValidationResult> getResultsWithCriticalIssues() {
        return validationResults.stream().filter(APIResponseValidationResult::hasCriticalIssues).collect(java.util.stream.Collectors.toList());
    }
    
    public List<APIFieldValidation> getAllInvalidFields() {
        List<APIFieldValidation> invalidFields = new ArrayList<>();
        for (APIResponseValidationResult result : validationResults) {
            invalidFields.addAll(result.getInvalidFieldValidations());
        }
        return invalidFields;
    }
    
    public List<APIFieldValidation> getAllMissingFields() {
        List<APIFieldValidation> missingFields = new ArrayList<>();
        for (APIResponseValidationResult result : validationResults) {
            missingFields.addAll(result.getMissingFieldValidations());
        }
        return missingFields;
    }
    
    public boolean hasAnyIssues() {
        return summary.getTotalInvalidFields() > 0 || summary.getTotalMissingFields() > 0;
    }
    
    public boolean hasCriticalIssues() {
        return summary.getTotalMissingFields() > 0 || summary.getResponsesWithCriticalIssues() > 0;
    }
    
    public double getOverallValidityPercentage() {
        return summary.getTotalFields() > 0 ? 
                (double) summary.getTotalValidFields() / summary.getTotalFields() * 100 : 100;
    }
    
    public double getOverallCompletenessPercentage() {
        return summary.getTotalFields() > 0 ? 
                (double) summary.getTotalPresentFields() / summary.getTotalFields() * 100 : 100;
    }
    
    public int getTotalResponsesAnalyzed() {
        return validationResults.size();
    }
    
    // Private helper methods
    private void updateSummary(APIResponseValidationResult result) {
        summary.incrementTotalResponses();
        summary.addFields(result.getTotalFields());
        summary.addValidFields(result.getValidFields());
        summary.addPresentFields(result.getPresentFields());
        summary.addFieldsWithValues(result.getFieldsWithValues());
        
        if (result.hasAnyIssues()) {
            summary.incrementResponsesWithIssues();
        }
        
        if (result.hasCriticalIssues()) {
            summary.incrementResponsesWithCriticalIssues();
        }
        
        if (result.isValid()) {
            summary.incrementValidResponses();
        }
    }
    
    @Override
    public String toString() {
        return String.format("APIResponseValidationReport{responses=%d, fields=%d, valid=%.1f%%, complete=%.1f%%, critical=%s}", 
                getTotalResponsesAnalyzed(), summary.getTotalFields(), 
                getOverallValidityPercentage(), getOverallCompletenessPercentage(), hasCriticalIssues());
    }
}