package com.xiuxian.game.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the results of API response validation
 * Aggregates field validations and provides summary information
 */
public class APIResponseValidationResult {
    
    private String responseClassName;
    private List<APIFieldValidation> fieldValidations;
    private List<String> errors;
    
    public APIResponseValidationResult() {
        this.fieldValidations = new ArrayList<>();
        this.errors = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getResponseClassName() {
        return responseClassName;
    }
    
    public void setResponseClassName(String responseClassName) {
        this.responseClassName = responseClassName;
    }
    
    public List<APIFieldValidation> getFieldValidations() {
        return fieldValidations;
    }
    
    public void setFieldValidations(List<APIFieldValidation> fieldValidations) {
        this.fieldValidations = fieldValidations;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    // Methods to add data
    public void addFieldValidation(APIFieldValidation fieldValidation) {
        fieldValidations.add(fieldValidation);
    }
    
    public void addError(String error) {
        errors.add(error);
    }
    
    // Analysis methods
    public int getTotalFields() {
        return fieldValidations.size();
    }
    
    public int getValidFields() {
        return (int) fieldValidations.stream().filter(APIFieldValidation::isValid).count();
    }
    
    public int getInvalidFields() {
        return getTotalFields() - getValidFields();
    }
    
    public int getPresentFields() {
        return (int) fieldValidations.stream().filter(APIFieldValidation::isPresent).count();
    }
    
    public int getMissingFields() {
        return getTotalFields() - getPresentFields();
    }
    
    public int getFieldsWithValues() {
        return (int) fieldValidations.stream().filter(APIFieldValidation::isHasValue).count();
    }
    
    public List<APIFieldValidation> getInvalidFieldValidations() {
        return fieldValidations.stream().filter(validation -> !validation.isValid()).collect(java.util.stream.Collectors.toList());
    }
    
    public List<APIFieldValidation> getMissingFieldValidations() {
        return fieldValidations.stream().filter(validation -> !validation.isPresent()).collect(java.util.stream.Collectors.toList());
    }
    
    public boolean isValid() {
        return getInvalidFields() == 0 && errors.isEmpty();
    }
    
    public boolean hasAnyIssues() {
        return getInvalidFields() > 0 || getMissingFields() > 0 || !errors.isEmpty();
    }
    
    public boolean hasCriticalIssues() {
        return getMissingFields() > 0 || !errors.isEmpty();
    }
    
    public double getValidityPercentage() {
        return getTotalFields() > 0 ? (double) getValidFields() / getTotalFields() * 100 : 100;
    }
    
    public double getCompletenessPercentage() {
        return getTotalFields() > 0 ? (double) getPresentFields() / getTotalFields() * 100 : 100;
    }
    
    @Override
    public String toString() {
        return String.format("APIResponseValidationResult{class='%s', fields=%d, valid=%d, present=%d, errors=%d}", 
                responseClassName, getTotalFields(), getValidFields(), getPresentFields(), errors.size());
    }
}