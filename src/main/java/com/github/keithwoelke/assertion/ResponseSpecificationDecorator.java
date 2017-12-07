package com.github.keithwoelke.assertion;

import com.google.common.collect.Lists;
import io.restassured.config.RestAssuredConfig;
import io.restassured.internal.ContentParser;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is designed to extend the behavior of the RestAssured ResponseSpecification class. It duplicates only
 * enough code to achieve the necessary modifications. All other functionality is delegated to the
 * ResponseSpecificationWrapper class. It primarily serves to return the validation results in a collection containing
 * both failed and successful results.
 * <p>
 * At this point in time, the successful validations contain an empty message. This could be changed if the need arises
 * for more detailed reporting.
 *
 * @author wkwoelke
 * @see ResponseSpecification
 * @see ResponseSpecificationWrapper
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
public class ResponseSpecificationDecorator {

    private static final Logger logger = LoggerFactory.getLogger(ResponseSpecificationDecorator.class);

    private final ResponseSpecificationWrapper responseSpecificationWrapper;
    private final HamcrestAssertionClosureDecorator assertionClosureDecorator;
    private final BodyMatcherGroupDecorator bodyMatcherGroupDecorator;

    public ResponseSpecificationDecorator(ResponseSpecification responseSpecification) {
        this.responseSpecificationWrapper = new ResponseSpecificationWrapper(responseSpecification);
        this.assertionClosureDecorator = new HamcrestAssertionClosureDecorator(this.responseSpecificationWrapper.assertionClosure);
        this.bodyMatcherGroupDecorator = new BodyMatcherGroupDecorator(this.responseSpecificationWrapper.bodyMatchers);
    }

    public boolean hasAssertionsDefined() {
        return responseSpecificationWrapper.hasAssertionsDefined();
    }

    private boolean hasBodyAssertionsDefined() {
        return responseSpecificationWrapper.hasBodyAssertionsDefined();
    }

    private boolean isEagerAssert() {
        return responseSpecificationWrapper.isEagerAssert();
    }

    public List<ValidationResult> validate(Response response) {
        return assertionClosureDecorator.validate(response);
    }

    @SuppressWarnings("unused")
    class HamcrestAssertionClosureDecorator {

        private final ResponseSpecificationWrapper.HamcrestAssertionClosureWrapper assertionClosureWrapper;

        public HamcrestAssertionClosureDecorator(ResponseSpecificationWrapper.HamcrestAssertionClosureWrapper assertionClosureWrapper) {
            this.assertionClosureWrapper = assertionClosureWrapper;
        }

        public List<ValidationResult> validate(Response response) {
            List<ValidationResult> validationResults = Lists.newArrayList();

            if (hasAssertionsDefined()) {
                validationResults.addAll(validateStatusCodeAndStatusLine(response));
                validationResults.addAll(validateHeadersAndCookies(response));
                validationResults.addAll(validateContentType(response));
                validationResults.addAll(validateResponseTime(response));

                if (hasBodyAssertionsDefined()) {
                    RestAssuredConfig cfg = responseSpecificationWrapper.config != null ? responseSpecificationWrapper.config : new RestAssuredConfig();

                    if (requiresPathParsing() && (!isEagerAssert() || responseSpecificationWrapper.contentParser == null)) {
                        responseSpecificationWrapper.contentParser = new ContentParser().parse(response, responseSpecificationWrapper.rpr, cfg, isEagerAssert());
                    }

                    validationResults.addAll(bodyMatcherGroupDecorator.validate(response, responseSpecificationWrapper.contentParser, cfg));
                }

                List<ValidationResult> errors = validationResults.stream().
                        filter(result -> !result.isSuccess()).
                        collect(Collectors.toList());
                int numberOfErrors = errors.size();
                if (numberOfErrors > 0) {
                    logRequestAndResponseIfEnabled();
                    List<String> errorMessages = errors.stream().map(ValidationResult::getMessage).collect(Collectors.toList());
                    String errorMessage = StringUtils.join(errorMessages, "\n");
                    String s = numberOfErrors > 1 ? "s" : "";

                    ValidationError validationError = new ValidationError(String.format("%d validation%s failed.%n%s", numberOfErrors, s, errorMessage));
                    validationError.setValidationResults(validationResults);

                    throw validationError;
                }
            }

            return validationResults;
        }

        private List<ValidationResult> validateStatusCodeAndStatusLine(Response response) {
            final int totalNumberOfStatusCodeValidations = responseSpecificationWrapper.expectedStatusCode == null ? 0 : 1;
            final int totalNumberOfStatusLineValidations = responseSpecificationWrapper.expectedStatusLine == null ? 0 : 1;
            final int totalNumberOfValidations = totalNumberOfStatusCodeValidations + totalNumberOfStatusLineValidations;

            List<Map<String, Object>> failures = assertionClosureWrapper.validateStatusCodeAndStatusLine(response);

            ValidationResult.ValidationResultListBuilder validationResultListBuilder = ValidationResult.listBuilder();
            validationResultListBuilder.hamcrestResults(failures);
            validationResultListBuilder.count(totalNumberOfValidations);

            return validationResultListBuilder.build();
        }

        private List<ValidationResult> validateHeadersAndCookies(Response response) {
            final int totalNumberOfHeaderValidations = responseSpecificationWrapper.headerAssertions.size();
            final int totalNumberOfCookieValidations = responseSpecificationWrapper.cookieAssertions.size();
            final int totalNumberOfValidations = totalNumberOfHeaderValidations + totalNumberOfCookieValidations;

            List<Map<String, Object>> failures = assertionClosureWrapper.validateHeadersAndCookies(response);

            ValidationResult.ValidationResultListBuilder validationResultListBuilder = ValidationResult.listBuilder();
            validationResultListBuilder.hamcrestResults(failures);
            validationResultListBuilder.count(totalNumberOfValidations);

            return validationResultListBuilder.build();
        }

        private List<ValidationResult> validateContentType(Response response) {
            List<Map<String, Object>> failures = assertionClosureWrapper.validateContentType(response);

            ValidationResult.ValidationResultListBuilder validationResultListBuilder = ValidationResult.listBuilder();
            validationResultListBuilder.hamcrestResults(failures);

            return validationResultListBuilder.build();
        }

        private List<ValidationResult> validateResponseTime(Response response) {
            List<Map<String, Object>> failures = assertionClosureWrapper.validateResponseTime(response);

            ValidationResult.ValidationResultListBuilder validationResultListBuilder = ValidationResult.listBuilder();
            validationResultListBuilder.hamcrestResults(failures);

            return validationResultListBuilder.build();
        }

        private boolean requiresPathParsing() {
            return assertionClosureWrapper.requiresPathParsing();
        }

        private void logRequestAndResponseIfEnabled() {
            assertionClosureWrapper.logRequestAndResponseIfEnabled();
        }
    }
}
