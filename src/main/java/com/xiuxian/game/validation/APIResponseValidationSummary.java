package com.xiuxian.game.validation;

/**
 * Summary statistics for API response validation across multiple response classes
 * Provides aggregate counts and percentages
 */
public class APIResponseValidationSummary {
    
    private int totalResponses;
    private int validResponses;
    private int responsesWithIssues;
    private int responsesWithCriticalIssues;
    
    private int totalFields;
    private int totalValidFields;
    private int totalPresentFields;
    private int totalFieldsWithValues;
    
    public APIResponseValidationSummary() {
        // Initialize all counters to 0
    }
    
    // Increment methods
    public void incrementTotalResponses() {
        totalResponses++;
    }
    
    public void incrementValidResponses() {
        validResponses++;
    }
    
    public void incrementResponsesWithIssues() {
        responsesWithIssues++;
    }
    
    public void incrementResponsesWithCriticalIssues() {
        responsesWithCriticalIssues++;
    }
    
    public void addFields(int count) {
        totalFields += count;
    }
    
    public void addValidFields(int count) {
        totalValidFields += count;
    }
    
    public void addPresentFields(int count) {
        totalPresentFields += count;
    }
    
    public void addFieldsWithValues(int count) {
        totalFieldsWithValues += count;
    }
    
    // Getters
    public int getTotalResponses() {
        return totalResponses;
    }
    
    public int getValidResponses() {
        return validResponses;
    }
    
    public int getResponsesWithIssues() {
        return responsesWithIssues;
    }
    
    public int getResponsesWithCriticalIssues() {
        return responsesWithCriticalIssues;
    }
    
    public int getTotalFields() {
        return totalFields;
    }
    
    public int getTotalValidFields() {
        return totalValidFields;
    }
    
    public int getTotalInvalidFields() {
        return totalFields - totalValidFields;
    }
    
    public int getTotalPresentFields() {
        return totalPresentFields;
    }
    
    public int getTotalMissingFields() {
        return totalFields - totalPresentFields;
    }
    
    public int getTotalFieldsWithValues() {
        return totalFieldsWithValues;
    }
    
    public int getTotalFieldsWithoutValues() {
        return totalPresentFields - totalFieldsWithValues;
    }
    
    // Calculated properties
    public double getValidResponsePercentage() {
        return totalResponses > 0 ? (double) validResponses / totalResponses * 100 : 100;
    }
    
    public double getResponsesWithIssuesPercentage() {
        return totalResponses > 0 ? (double) responsesWithIssues / totalResponses * 100 : 0;
    }
    
    public double getValidFieldPercentage() {
        return totalFields > 0 ? (double) totalValidFields / totalFields * 100 : 100;
    }
    
    public double getFieldCompletenessPercentage() {
        return totalFields > 0 ? (double) totalPresentFields / totalFields * 100 : 100;
    }
    
    public double getFieldValuePercentage() {
        return totalPresentFields > 0 ? (double) totalFieldsWithValues / totalPresentFields * 100 : 100;
    }
    
    public boolean isHealthy() {
        return responsesWithCriticalIssues == 0 && getTotalInvalidFields() == 0 && getTotalMissingFields() == 0;
    }
    
    public boolean hasAnyIssues() {
        return responsesWithIssues > 0 || getTotalInvalidFields() > 0 || getTotalMissingFields() > 0;
    }
    
    public boolean hasCriticalIssues() {
        return responsesWithCriticalIssues > 0 || getTotalMissingFields() > 0;
    }
    
    @Override
    public String toString() {
        return String.format("APIResponseValidationSummary{responses=%d (valid=%d, issues=%d, critical=%d), fields=%d (valid=%.1f%%, complete=%.1f%%), healthy=%s}", 
                totalResponses, validResponses, responsesWithIssues, responsesWithCriticalIssues, totalFields, 
                getValidFieldPercentage(), getFieldCompletenessPercentage(), isHealthy());
    }
}