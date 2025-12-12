package com.xiuxian.game.validation;

import java.util.List;

/**
 * Exception thrown when validation fails
 * Contains detailed information about validation failures
 */
public class ValidationException extends RuntimeException {
    
    private final List<TypeMismatch> mismatches;
    
    public ValidationException(String message) {
        super(message);
        this.mismatches = null;
    }
    
    public ValidationException(String message, List<TypeMismatch> mismatches) {
        super(message);
        this.mismatches = mismatches;
    }
    
    public ValidationException(String message, List<TypeMismatch> mismatches, Throwable cause) {
        super(message, cause);
        this.mismatches = mismatches;
    }
    
    public List<TypeMismatch> getMismatches() {
        return mismatches;
    }
    
    public boolean hasMismatches() {
        return mismatches != null && !mismatches.isEmpty();
    }
    
    public int getMismatchCount() {
        return mismatches != null ? mismatches.size() : 0;
    }
}