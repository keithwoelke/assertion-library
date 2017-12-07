package com.github.keithwoelke.assertion;

import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the base assertion library which interfaces with Hamcrest and RestAssured. It performs assertions or
 * expectations and returns the results. It does not record results nor should it throw any exceptions. For that
 * extended behavior, the AssertionRecorder class should be used.
 *
 * @author wkwoelke
 */
@SuppressWarnings("WeakerAccess")
@Service
@Slf4j
public class BaseValidationLibrary {

    private final RestAssuredObjectBuilder restAssuredObjectBuilder;

    @Autowired
    BaseValidationLibrary(RestAssuredObjectBuilder restAssuredObjectBuilder) {
        this.restAssuredObjectBuilder = restAssuredObjectBuilder;
    }

    <T> ValidationResult assertThat(String reason, T actual, Matcher<? super T> matcher) {
        ValidationResult.ValidationResultBuilder validationResultBuilder = ValidationResult.builder();
        validationResultBuilder.success(true);
        validationResultBuilder.type(ValidationType.ASSERTION);

        try {
            if (StringUtils.isEmpty(reason)) {
                MatcherAssert.assertThat(actual, matcher);
            } else {
                MatcherAssert.assertThat(reason, actual, matcher);
            }
        } catch (AssertionError e) {
            validationResultBuilder.success(false);
            validationResultBuilder.message(e.getMessage());
            validationResultBuilder.stackTrace(ValidationReportGenerator.getStacktrace());
        }

        return validationResultBuilder.build();
    }

    <T> ValidationResult assertThat(T actual, Matcher<? super T> matcher) {
        return assertThat(null, actual, matcher);
    }

    List<ValidationResult> assertThat(Response response, ResponseSpecification responseSpecification) {
        ResponseSpecificationDecorator responseSpecificationDecorator = restAssuredObjectBuilder
                .getResponseSpecificationDecorator(responseSpecification);
        List<ValidationResult> validations;
        ValidationResult.ValidationResultBuilder validationResultBuilder = ValidationResult.builder();

        try {
            validations = responseSpecificationDecorator.validate(response);
        } catch (ValidationError e) {
            validations = e.getValidationResults();
            validationResultBuilder.stackTrace(ValidationReportGenerator.getStacktrace());
        }

        validations = validations.stream().
                map(validation -> {
                    validationResultBuilder.success(validation.isSuccess());
                    validationResultBuilder.message(validation.getMessage());
                    validationResultBuilder.eventTime(validation.getEventTime());
                    validationResultBuilder.type(ValidationType.ASSERTION);

                    return validationResultBuilder.build();
                }).
                collect(Collectors.toList());

        return validations;
    }

    <T> ValidationResult expectThat(String reason, T actual, Matcher<? super T> matcher) {
        ValidationResult.ValidationResultBuilder validationResultBuilder = ValidationResult.builder();
        validationResultBuilder.success(true);
        validationResultBuilder.type(ValidationType.EXPECTATION);

        try {
            if (StringUtils.isEmpty(reason)) {
                MatcherAssert.assertThat(actual, matcher);
            } else {
                MatcherAssert.assertThat(reason, actual, matcher);
            }
        } catch (AssertionError e) {
            validationResultBuilder.success(false);
            validationResultBuilder.stackTrace(ValidationReportGenerator.getStacktrace());
            validationResultBuilder.message(e.getMessage());
        }

        return validationResultBuilder.build();
    }

    <T> ValidationResult expectThat(T actual, Matcher<? super T> matcher) {
        return expectThat(null, actual, matcher);
    }

    List<ValidationResult> expectThat(Response response, ResponseSpecification responseSpecification) {
        ResponseSpecificationDecorator responseSpecificationDecorator = restAssuredObjectBuilder
                .getResponseSpecificationDecorator(responseSpecification);
        List<ValidationResult> validations;
        ValidationResult.ValidationResultBuilder validationResultBuilder = ValidationResult.builder();

        try {
            validations = responseSpecificationDecorator.validate(response);
        } catch (ValidationError e) {
            validations = e.getValidationResults();
            validationResultBuilder.stackTrace(ValidationReportGenerator.getStacktrace());
        }

        validations = validations.stream().
                map(validation -> {
                    validationResultBuilder.success(validation.isSuccess());
                    validationResultBuilder.message(validation.getMessage());
                    validationResultBuilder.eventTime(validation.getEventTime());
                    validationResultBuilder.stackTrace(ValidationReportGenerator.getStacktrace());
                    validationResultBuilder.type(ValidationType.EXPECTATION);

                    return validationResultBuilder.build();
                }).
                collect(Collectors.toList());

        return validations;
    }
}
