package com.github.keithwoelke.assertion;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.List;

/**
 * This TestNG listener can be used to trigger test failures on expectation failure where a test would otherwise pass,
 * provide useful stack traces both both expectations and assertions, and provide reporting/metrics on a per test case
 * basis as well as at the completion of a test run.
 *
 * @author wkwoelke
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Slf4j
public class TestNGAssertionListener implements ITestListener {

    public static final String REASON_KEY = "reason";

    private static ValidationRepository validationRepository;
    private static ValidationReportGenerator validationReportGenerator;

    @Override
    public void onTestStart(ITestResult result) {
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long threadId = Thread.currentThread().getId();

        List<ValidationResult> validations = getValidationRepository().getValidations(threadId);
        boolean isFailedTest = ValidationListUtils.hasFailure(validations);

        if (isFailedTest) {
            failTest(result);
        }

        exportResults(result);
        logTestSummary(threadId);
        archiveResults(threadId);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        long threadId = Thread.currentThread().getId();

        failTest(result);

        exportResults(result);
        logTestSummary(threadId);
        archiveResults(threadId);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    @Override
    public void onStart(ITestContext context) {
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info(String.format("Final Summary: %s", getValidationReportGenerator().getValidationSummary()));
    }

    private void archiveResults(long threadId) {
        getValidationRepository().archiveResults(threadId);
    }

    private void exportResults(ITestResult result) {
        long threadId = Thread.currentThread().getId();

        String reason = null;
        if (result.getAttribute(REASON_KEY) != null) {
            reason = (String) result.getAttribute(REASON_KEY);
        }

        String results = "";

        if (result.getThrowable() != null) {
            results = result.getThrowable().getMessage();
        }

        if (reason == null) {
            reason = results;
        } else {
            reason = String.format("%s%n%n%s", results, reason);
        }

        result.setAttribute(REASON_KEY, reason);
    }

    private void failTest(ITestResult result) {
        long threadId = Thread.currentThread().getId();

        ITestContext context = result.getTestContext();
        context.getFailedTests().addResult(result, result.getMethod());
        context.getPassedTests().removeResult(result);
        result.setStatus(ITestResult.FAILURE);

        List<ValidationResult> failedValidations = getValidationRepository().getFailedValidations(threadId);
        String combinedMessages = getValidationReportGenerator().getConsolidatedMessages(failedValidations);
        StringBuilder fullReport = new StringBuilder();

        Throwable assertionThrowable = result.getThrowable();

        if (assertionThrowable != null) {
            fullReport.append(String.format("Execution halted prematurely:%n%n%s", ExceptionUtils.getStackTrace
                    (assertionThrowable)));
        }

        if (!StringUtils.isEmpty(combinedMessages)) {
            fullReport.append(String.format("%s%n%nExpected failures:%n%n%s%n", fullReport,
                    combinedMessages));
        }

        ValidationError throwable = new ValidationError(fullReport.toString());
        throwable.setStackTrace(new StackTraceElement[]{});
        result.setThrowable(throwable);
    }

    private ValidationReportGenerator getValidationReportGenerator() {
        if (validationReportGenerator == null) {
            log.error("Validation report generator is null. Make sure to properly import the assertion-core " +
                    "Spring configuration.");
        }

        return validationReportGenerator;
    }

    private ValidationRepository getValidationRepository() {
        if (validationRepository == null) {
            log.error("Validation repository is null. Make sure to properly import the assertion-core Spring " +
                    "configuration.");
        }

        return validationRepository;
    }

    private void logTestSummary(long threadId) {
        log.info(String.format("Test Summary: %s", getValidationReportGenerator().getValidationSummary(threadId)));
    }

    @Autowired
    public void setAssertionReporter(ValidationReportGenerator validationReportGenerator) {
        TestNGAssertionListener.validationReportGenerator = validationReportGenerator;
    }

    @Autowired
    public void setAssertionReporter(ValidationRepository validationRepository) {
        TestNGAssertionListener.validationRepository = validationRepository;
    }
}