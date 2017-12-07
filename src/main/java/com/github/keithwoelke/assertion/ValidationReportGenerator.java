package com.github.keithwoelke.assertion;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is responsible for providing human readable validation results. Its methods either query the ValidationRepository for a key and return a
 * validation report, or they will take a List of ValidationResult objects from which they will provide a report or summary.
 *
 * @author wkwoelke
 */
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
@Service
public class ValidationReportGenerator {

    private final String NO_VALIDATIONS_WERE_EXECUTED = "No validations were executed.";
    private final String EXPECTATION_SUMMARY_MESSAGE = "%s of %s expectation%s";
    private final String ASSERTION_SUMMARY_MESSAGE = "%s of %s assertion%s";

    private final ValidationRepository validationRepository;

    @Autowired
    public ValidationReportGenerator(ValidationRepository validationRepository) {
        this.validationRepository = validationRepository;
    }

    public static StackTraceElement[] getStacktrace() {
        ArrayList<StackTraceElement> stackTraceElements = Lists.newArrayList(Thread.currentThread().getStackTrace());
        ArrayList<StackTraceElement> modifiedStackTraceElements = Lists.newArrayList();
        ArrayList<String> stackFramesToSkip = Lists.newArrayList(("com.sinclairdigital.qa.assertion" + ".BaseValidationLibrary").toLowerCase(),
                "AssertionRecorder".toLowerCase(), "ValidationReportGenerator"
                        .toLowerCase());

        boolean skip = false;
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (!skip && stackFramesToSkip.contains(stackTraceElement.getClassName().toLowerCase()) || !StringUtils.containsIgnoreCase
                    (stackTraceElement.getClassName(), "com.sinclairdigital")) {
                continue;
            }

            skip = true;
            modifiedStackTraceElements.add(stackTraceElement);
        }

        return modifiedStackTraceElements.toArray(new StackTraceElement[]{});
    }

    private String formatNumber(int number) {
        return NumberFormat.getIntegerInstance().format(number);
    }

    String getConsolidatedMessages(List<ValidationResult> validations) {
        Collections.sort(validations);
        String[] messages = validations.stream().
                map(ValidationResult::getMessage).
                toArray(String[]::new);

        return StringUtils.join(messages, "\n");
    }

    public String getValidationFailureReport(List<ValidationResult> validations) {
        Collections.sort(validations);
        String validationReport = getValidationSummary(validations);

        List<ValidationResult> failedValidations = ValidationListUtils.getFailed(validations);
        validationReport += "\n" + getConsolidatedMessages(failedValidations);

        return validationReport;
    }

    public String getValidationFailureReport(Object key) {
        List<ValidationResult> validations = validationRepository.getValidations(key);

        return getValidationFailureReport(validations);
    }

    public String getValidationFailureReport(ValidationResult validationResult) {
        return getValidationFailureReport(Lists.newArrayList(validationResult));
    }

    public String getValidationSummary(List<ValidationResult> validations) {
        String validationSummary = "";

        List<ValidationResult> assertions = ValidationListUtils.getAssertions(validations);
        List<ValidationResult> expectations = ValidationListUtils.getExpectations(validations);
        List<ValidationResult> failedAssertions = ValidationListUtils.getFailed(assertions);
        List<ValidationResult> failedExpectations = ValidationListUtils.getFailed(expectations);
        List<ValidationResult> passedAssertions = ValidationListUtils.getPassed(assertions);
        List<ValidationResult> passedExpectations = ValidationListUtils.getPassed(expectations);

        String expectationPlural = expectations.size() != 1 ? "s" : "";
        String assertionPlural = assertions.size() != 1 ? "s" : "";

        int numExpectationsPassedOrFailed;
        int numAssertionsPassedOrFailed;
        String passStatus;
        if (ValidationListUtils.hasFailure(assertions) || ValidationListUtils.hasFailure(expectations)) {
            numAssertionsPassedOrFailed = failedAssertions.size();
            numExpectationsPassedOrFailed = failedExpectations.size();
            passStatus = " failed.";
        } else {
            numAssertionsPassedOrFailed = passedAssertions.size();
            numExpectationsPassedOrFailed = passedExpectations.size();
            passStatus = " passed successfully.";
        }

        if (expectations.size() > 0) {
            validationSummary += String.format(EXPECTATION_SUMMARY_MESSAGE, formatNumber(numExpectationsPassedOrFailed), formatNumber(expectations
                    .size()), expectationPlural);

            if (assertions.size() > 0) {
                validationSummary += " and ";
            }
        }

        if (assertions.size() > 0) {
            validationSummary += String.format(ASSERTION_SUMMARY_MESSAGE, formatNumber(numAssertionsPassedOrFailed), formatNumber(assertions.size()
            ), assertionPlural);
        }

        if (!StringUtils.isEmpty(validationSummary)) {
            validationSummary += passStatus;
        } else {
            validationSummary = NO_VALIDATIONS_WERE_EXECUTED;
        }

        return validationSummary;
    }

    public String getValidationSummary() {
        List<ValidationResult> validations = validationRepository.getAllValidations();

        return getValidationSummary(validations);
    }

    public String getValidationSummary(Object key) {
        return getValidationSummary(validationRepository.getValidations(key));
    }
}
