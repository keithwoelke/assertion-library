package com.github.keithwoelke.assertion;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class provides some commonly used utility methods for operating on ValidationResult Lists.
 *
 * @author wkwoelke
 */
@SuppressWarnings("WeakerAccess")
public class ValidationListUtils {

    private ValidationListUtils() {
        throw new AssertionError();
    }

    public static List<ValidationResult> getAssertions(List<ValidationResult> validations) {
        return validations.stream().
                filter(validation -> validation.getType() != null).
                filter(validation -> validation.getType().equals(ValidationType.ASSERTION)).
                collect(Collectors.toList());
    }

    public static List<ValidationResult> getExpectations(List<ValidationResult> validations) {
        return validations.stream().
                filter(validation -> validation.getType() != null).
                filter(validation -> validation.getType().equals(ValidationType.EXPECTATION)).
                collect(Collectors.toList());
    }

    public static List<ValidationResult> getFailed(List<ValidationResult> validations) {
        if (validations == null || validations.isEmpty()) {
            return Lists.newArrayList();
        }

        return validations.stream().
                filter(result -> !result.isSuccess()).
                collect(Collectors.toList());
    }

    public static List<ValidationResult> getPassed(List<ValidationResult> validations) {
        if (validations == null || validations.isEmpty()) {
            return Lists.newArrayList();
        }

        return validations.stream().
                filter(ValidationResult::isSuccess).
                collect(Collectors.toList());
    }

    static boolean hasFailure(List<ValidationResult> validations) {
        return ValidationListUtils.getFailed(validations).size() > 0;
    }
}
