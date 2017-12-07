package com.github.keithwoelke.assertion;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * This is the equivalent of an AssertionError extended to support and return detailed validation results.
 *
 * @author wkwoelke
 */
@SuppressWarnings("WeakerAccess")
@Getter
@Setter
public class ValidationError extends AssertionError {

    private List<ValidationResult> validationResults = Lists.newArrayList();

    public ValidationError(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationError(String message) {
        super(message);
    }

    public void addValidationResults(List<ValidationResult> validationResults) {
        this.validationResults.addAll(validationResults);
    }

    public void addValidationResults(ValidationResult... validationResults) {
        addValidationResults(Lists.newArrayList(validationResults));
    }
}
