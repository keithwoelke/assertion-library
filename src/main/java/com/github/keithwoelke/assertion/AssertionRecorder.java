package com.github.keithwoelke.assertion;

import com.google.common.collect.Lists;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hamcrest.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.testng.annotations.Listeners;

import java.util.List;

/**
 * This is the primary class for asserting/expecting things. It is responsible for recording the results of both
 * assertions and expectations, as well as throwing ValidationErrors when assertions fail. The expectations and
 * assertions are recorded in the ValidationRepository
 * <p>
 * An assertion should be thought of in the traditional sense. If an assertion fails, the intent is that test execution
 * should halt immediately. On the other hand, an expectation can be thought of as a "soft" assertion where the intent
 * is that in failure should be recorded, but no failure should be immediately triggered as a result.
 * <p>
 * The net effect of this behavior is that it is up to the implementation/test runner to determine whether or not to
 * invoke a test failure as a result of an expectation failing.
 * <p>
 * Both assertions and expectations work in the same manner as Hamcrest assertThat and support the same matchers. Each
 * of these style validations will yield either a single success or failure.
 * <p>
 * Additional overrides are provided to support RestAssured validations in the same manner as assertThat, only a
 * RestAssured ResponseSpecification is provided instead of a matcher and "actual" is a RestAssured Response. These
 * methods can be expected to yield a list of validation results for either assertion or expectation.
 *
 * @author wkwoelke
 * @see TestNGAssertionListener
 * @see org.hamcrest.MatcherAssert#assertThat(Object, Matcher)
 * @see ResponseSpecification
 * @see Response
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Listeners({TestNGAssertionListener.class})
@Slf4j
@Service
public class AssertionRecorder {

    private final ValidationRepository validationRepository;
    private final BaseValidationLibrary baseValidationLibrary;
    private final ValidationReportGenerator validationReportGenerator;

    @Autowired
    public AssertionRecorder(BaseValidationLibrary baseValidationLibrary, ValidationRepository validationRepository,
            ValidationReportGenerator validationReportGenerator) {
        this.baseValidationLibrary = baseValidationLibrary;
        this.validationRepository = validationRepository;
        this.validationReportGenerator = validationReportGenerator;
    }

    /**
     * Externally, this function behaves identical to the Hamcrest assertThat of the same signature. It will record any
     * assertion results in the ValidationRepository regardless of whether they are a success or a failure. If the
     * validation fails, it will throw a ValidationError with additional detail.
     *
     * @see org.hamcrest.MatcherAssert#assertThat(Object, Matcher)
     */
    public <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {
        ValidationResult validationResult = baseValidationLibrary.assertThat(reason, actual, matcher);
        recordValidations(validationResult);

        if (!validationResult.isSuccess()) {
            String assertionFailureReport = validationReportGenerator.getValidationFailureReport(validationResult);
            ValidationError validationError = new ValidationError(assertionFailureReport);
            validationError.addValidationResults(validationResult);

            throw validationError;
        }
    }

    /**
     * @see #assertThat(String, Object, Matcher)
     */
    public <T> void assertThat(T actual, Matcher<? super T> matcher) {
        assertThat(null, actual, matcher);
    }

    /**
     * Works similar to the Hamcrest assertThat, but designed specifically for RestAssured objects. In place of the
     * Hamcrest actual, this method takes a RestAssured Response object. In place of a Hamcrest matcher, this method
     * takes a RestAssured ResponseSpecification.
     *
     * @param response              a RestAssured Response object
     * @param responseSpecification a RestAssured ResponseSpecification object
     * @see ResponseSpecification
     * @see Response
     */
    public void assertThat(String reason, Response response, ResponseSpecification responseSpecification) {
        List<ValidationResult> validationResults = baseValidationLibrary.assertThat(response, responseSpecification);
        recordValidations(validationResults);

        if (ValidationListUtils.getFailed(validationResults).size() > 0) {
            String assertionFailureReport = validationReportGenerator.getValidationFailureReport(validationResults);

            if (!StringUtils.isEmpty(reason)) {
                assertionFailureReport = String.format("%s%n%n%s", reason, assertionFailureReport);
            }

            ValidationError validationError = new ValidationError(assertionFailureReport);
            validationError.addValidationResults(validationResults);
            validationError.setStackTrace(validationResults.get(0).getStackTrace());

            throw validationError;
        }
    }

    /**
     * @see #assertThat(String, Response, ResponseSpecification)
     */
    public void assertThat(Response response, ResponseSpecification responseSpecification) {
        assertThat(null, response, responseSpecification);
    }

    /**
     * Externally, this function behaves nearly identically to the Hamcrest assertThat of the same signature. It will
     * record any expectation results in the ValidationRepository regardless of whether they are a success or a failure.
     * If the validation fails, this method will <b>not</b> throw an error. It will simply record the result for later
     * evaluation.
     *
     * @see org.hamcrest.MatcherAssert#assertThat(Object, Matcher)
     * @see TestNGAssertionListener
     */
    public <T> void expectThat(String reason, T actual, Matcher<? super T> matcher) {
        ValidationResult validationResult = baseValidationLibrary.expectThat(reason, actual, matcher);
        recordValidations(validationResult);

        if (!validationResult.isSuccess()) {
            String failedExpectationReport = validationReportGenerator.getValidationFailureReport(validationResult);
            ValidationError validationError = new ValidationError(failedExpectationReport);
            validationError.setStackTrace(validationResult.getStackTrace());

            if (log.isDebugEnabled()) {
                log.debug(ExceptionUtils.getStackTrace(validationError));
            } else {
                log.warn(failedExpectationReport);
            }
        }
    }

    /**
     * @see #expectThat(String, Object, Matcher)
     */
    public <T> void expectThat(T actual, Matcher<? super T> matcher) {
        expectThat(null, actual, matcher);
    }

    /**
     * Works similar to the Hamcrest assertThat, but designed specifically for RestAssured objects. In place of the
     * Hamcrest actual, this method takes a RestAssured Response object. In place of a Hamcrest matcher, this method
     * takes a RestAssured ResponseSpecification. If the validation fails, this method will <b>not</b> throw an error.
     * It will simply record the result for later evaluation.
     *
     * @param response              a RestAssured Response object
     * @param responseSpecification a RestAssured ResponseSpecification object
     * @see ResponseSpecification
     * @see Response
     */
    public void expectThat(String reason, Response response, ResponseSpecification responseSpecification) {
        List<ValidationResult> validationResults = baseValidationLibrary.expectThat(response, responseSpecification);
        recordValidations(validationResults);

        if (ValidationListUtils.getFailed(validationResults).size() > 0) {
            String failedExpectationReport = validationReportGenerator.getValidationFailureReport(validationResults);

            if (!StringUtils.isEmpty(reason)) {
                failedExpectationReport = String.format("%s%n%n%s", reason, failedExpectationReport);
            }

            ValidationError validationError = new ValidationError(failedExpectationReport);
            validationError.addValidationResults(validationResults);
            validationError.setStackTrace(validationResults.get(0).getStackTrace());

            if (log.isDebugEnabled()) {
                log.debug(ExceptionUtils.getStackTrace(validationError));
            } else {
                log.warn(failedExpectationReport);
            }
        }
    }

    /**
     * @see #expectThat(String, Response, ResponseSpecification)
     */
    public void expectThat(Response response, ResponseSpecification responseSpecification) {
        expectThat(null, response, responseSpecification);
    }

    /**
     * Manually record validations results. If a ValidationResult has no type, it will be ignored.
     *
     * @param validationResults the list of validation results to record
     */
    public void recordValidations(List<ValidationResult> validationResults) {
        long threadId = Thread.currentThread().getId();

        validationRepository.addAssertions(threadId, ValidationListUtils.getAssertions(validationResults));
        validationRepository.addExpectations(threadId, ValidationListUtils.getExpectations(validationResults));
    }

    /**
     * @see AssertionRecorder#recordValidations(List)
     */
    public void recordValidations(ValidationResult... assertions) {
        recordValidations(Lists.newArrayList(assertions));
    }
}
