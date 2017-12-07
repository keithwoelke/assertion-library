package com.github.keithwoelke.assertion;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A ValidationResult contains all useful information about a validation. Specifically, it stores whether or not the
 * validation was successful, the associated message, time of the validation action, the type of validation (expectation
 * or assertion), and the stack track where the validation occurred.
 *
 * @author wkwoelke
 */
@Getter
@AllArgsConstructor
public class ValidationResult implements Comparable<ValidationResult> {

    private final boolean success;
    private final String message;
    private final LocalDateTime eventTime;
    private final ValidationType type;
    private final StackTraceElement[] stackTrace;

    public static ValidationResultBuilder builder() {
        return new ValidationResultBuilder();
    }

    public static ValidationResultListBuilder listBuilder() {
        return new ValidationResultListBuilder();
    }

    @Override
    public int compareTo(@Nonnull ValidationResult otherValidationResult) {
        if (this.eventTime.isBefore(otherValidationResult.eventTime)) {
            return -1;
        } else if (this.eventTime.isAfter(otherValidationResult.eventTime)) {
            return 1;
        } else {
            return 0;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    @ToString
    public static class ValidationResultBuilder {

        private boolean success;
        private String message = StringUtils.EMPTY;
        private LocalDateTime eventTime;
        private ValidationType type;
        private StackTraceElement[] stackTrace;

        public ValidationResultBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public ValidationResultBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ValidationResultBuilder eventTime(LocalDateTime eventTime) {
            this.eventTime = eventTime;
            return this;
        }

        public ValidationResultBuilder type(ValidationType type) {
            this.type = type;
            return this;
        }

        public ValidationResultBuilder stackTrace(StackTraceElement[] stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        public ValidationResult build() {
            if (eventTime == null) {
                eventTime = LocalDateTime.now();
            }

            return new ValidationResult(success, message, eventTime, type, stackTrace);
        }
    }

    @SuppressWarnings({"UnusedReturnValue", "unused"})
    @ToString
    public static class ValidationResultListBuilder {

        private final List<ValidationResult> validationResults = Lists.newArrayList();
        private int count;

        public ValidationResultListBuilder validationsResults(ValidationResult... validations) {
            validationsResults(Lists.newArrayList(validations));
            return this;
        }

        public ValidationResultListBuilder validationsResults(List<ValidationResult> validationResults) {
            this.validationResults.addAll(validationResults);
            return this;
        }

        public ValidationResultListBuilder hamcrestResults(List<Map<String, Object>> failures) {
            failures.
                    forEach(result -> {
                        ValidationResultBuilder validationResultBuilder = ValidationResult.builder();
                        validationResultBuilder.success((boolean) result.get("success"));
                        validationResultBuilder.message(result.get("errorMessage").toString());

                        validationResults.add(validationResultBuilder.build());
                    });

            return this;
        }

        public ValidationResultListBuilder count(int count) {
            this.count = count;
            return this;
        }

        public List<ValidationResult> build() {
            ValidationResult.ValidationResultBuilder validationResultBuilder = ValidationResult.builder();
            validationResultBuilder.success(true);

            List<ValidationResult> passPlaceholders = Collections.nCopies(count - this.validationResults.size(), validationResultBuilder.build());
            validationResults.addAll(passPlaceholders);

            return validationResults;
        }
    }
}
